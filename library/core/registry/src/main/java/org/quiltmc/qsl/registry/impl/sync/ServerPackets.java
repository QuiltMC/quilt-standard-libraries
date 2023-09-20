/*
 * Copyright 2022 The Quilt Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.registry.impl.sync;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.registry.impl.sync.mod_protocol.ModProtocolDef;
import org.quiltmc.qsl.registry.impl.sync.registry.RegistryFlag;
import org.quiltmc.qsl.registry.impl.sync.registry.SynchronizedRegistry;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * Identifiers of packets sent by server.
 */
@ApiStatus.Internal
public final class ServerPackets {
	/**
	 * Starts registry sync.
	 *
	 * <pre><code>
	 * {
	 *   Supported Versions: IntList
	 * }
	 * </code></pre>
	 */
	public static final Identifier HANDSHAKE = id("registry_sync/handshake");
	record Handshake(int version) implements CustomPayload {
		@Override
		public void write(PacketByteBuf buf) {
			buf.writeVarInt(version);
		}

		@Override
		public Identifier id() {
			return HANDSHAKE;
		}
	}

	/**
	 * Ends registry sync. No data
	 */
	public static final Identifier END = id("registry_sync/end");
	record End() implements CustomPayload {
		@Override
		public void write(PacketByteBuf buf) {
		}

		@Override
		public Identifier id() {
			return END;
		}
	}

	/**
	 * Sets current registry for next {@link ServerPackets#REGISTRY_DATA} and {@link ServerPackets#REGISTRY_RESTORE} packets.
	 *
	 * <pre><code>
	 * {
	 *   Registry identifier: Identifier
	 *   Count of entries: VarInt
	 *   Flags: byte
	 * }
	 * </code></pre>
	 */
	public static final Identifier REGISTRY_START = id("registry_sync/registry_start");
	@SuppressWarnings("unchecked")
	record RegistryStart<T extends Registry<?>>(T registry) implements CustomPayload {
		@Override
		public void write(PacketByteBuf buf) {
			// Registry id
			buf.writeIdentifier(((Registry<T>) Registries.REGISTRY).getId(registry));
			// Number of entries
			buf.writeVarInt(registry.size());

			// Registry flags
			var flag = ((SynchronizedRegistry<T>) registry).quilt$getRegistryFlag();
			if (((SynchronizedRegistry<T>) registry).quilt$getContentStatus() == SynchronizedRegistry.Status.OPTIONAL) {
				flag |= (byte) (0x1 << RegistryFlag.OPTIONAL.ordinal());
			}
			buf.writeByte(flag);
		}

		@Override
		public Identifier id() {
			return REGISTRY_START;
		}
	}

	/**
	 * This packet transmits registry data required for sync.
	 *
	 * <pre><code>
	 * {
	 *   Count of Namespaces: VarInt
	 *   [
	 *     Common Namespace: String
	 *     Count of Entries: VarInt
	 *     [
	 *       Path: String
	 *       Id: VarInt
	 *       Flags: byte
	 *     ]
	 *   ]
	 * }
	 * </code></pre>
	 */
	public static final Identifier REGISTRY_DATA = id("registry_sync/registry_data");
	record RegistryData(Map<String, ArrayList<SynchronizedRegistry.SyncEntry>> packetData) implements CustomPayload {
		@Override
		public void write(PacketByteBuf buf) {
			buf.writeVarInt(packetData.size());
			for (var key : packetData.keySet()) {
				var list = packetData.get(key);

				// Namespace
				buf.writeString(key);

				// Number of entries
				buf.writeVarInt(list.size());

				for (var entry : list) {
					// Path
					buf.writeString(entry.path());
					// Raw id from registry
					buf.writeVarInt(entry.rawId());
					// Entry flags
					buf.writeByte(entry.flags());
				}
			}
		}

		@Override
		public Identifier id() {
			return REGISTRY_DATA;
		}
	}

	/**
	 * Applies changes to current registry, doesn't have any data.
	 */
	public static final Identifier REGISTRY_APPLY = id("registry_sync/registry_apply");
	record RegistryApply() implements CustomPayload {
		@Override
		public void write(PacketByteBuf buf) {
		}

		@Override
		public Identifier id() {
			return REGISTRY_APPLY;
		}
	}

	/**
	 * This packet requests client to validate ids of {@link net.minecraft.block.Block#STATE_IDS} to prevent id mismatch.
	 * It might not send all states in single packet! It needs to be verified as is (aka, ids matter, count doesn't).
	 *
	 * <pre><code>
	 * {
	 *   Count of Entries: VarInt
	 *   [
	 *     Block Id: VarInt
	 *     Count of Entries: VarInt
	 *     [
	 *       BlockState Id: VarInt
	 *     ]
	 *   ]
	 * }
	 * </code></pre>
	 */
	public static final Identifier VALIDATE_BLOCK_STATES = id("registry_sync/validate/block_states");
	record ValidateStates(StateType type, Int2ObjectArrayMap<IntList> packetData) implements CustomPayload {
		@Override
		public void write(PacketByteBuf buf) {
			buf.writeVarInt(packetData.size());
			for (var entry : packetData.int2ObjectEntrySet()) {
				buf.writeVarInt(entry.getIntKey());
				buf.writeIntList(entry.getValue());
			}
		}

		@Override
		public Identifier id() {
			return HANDSHAKE;
		}

		enum StateType {
			BLOCK(VALIDATE_BLOCK_STATES), FLUID(VALIDATE_FLUID_STATES);

			public final Identifier packetId;

			StateType(Identifier packetId) {
				this.packetId = packetId;
			}
		}
	}

	/**
	 * Same structure as {@link ServerPackets#VALIDATE_BLOCK_STATES}, but for FluidStates
	 */
	public static final Identifier VALIDATE_FLUID_STATES = id("registry_sync/validate/fluid_states");

	/**
	 * Applies changes to current registry, doesn't have any data.
	 */
	public static final Identifier REGISTRY_RESTORE = id("registry_sync/registry_restore");
	record RegistryRestore() implements CustomPayload {
		@Override
		public void write(PacketByteBuf buf) {
		}

		@Override
		public Identifier id() {
			return HANDSHAKE;
		}
	}

	/**
	 * This packet sets failure text look/properties.
	 * Requires protocol version 3 or newer.
	 *
	 * <pre><code>
	 * {
	 *   Text Header: Text (String)
	 *   Text Footer: Text (String)
	 *   Show Details: bool
	 *
	 * }
	 * </code></pre>
	 */
	public static final Identifier ERROR_STYLE = id("registry_sync/error_style");
	record ErrorStyle(Text errorHeader, Text errorFooter, boolean showError) implements CustomPayload {
		@Override
		public void write(PacketByteBuf buf) {
			buf.writeText(errorHeader);
			buf.writeText(errorFooter);
			buf.writeBoolean(showError);
		}

		@Override
		public Identifier id() {
			return ERROR_STYLE;
		}
	}

	/**
	 * This packet requests client to validate and return supported Mod Protocol versions.
	 *
	 * <pre><code>
	 * {
	 *   Prioritized Id: String
	 *   Count of Entries: VarInt
	 *   [
	 *     Id: String
	 *     Name: String
	 *     Supported Versions: IntList
	 *     Optional: boolean
	 *   ]
	 * }
	 * </code></pre>
	 */
	public static final Identifier MOD_PROTOCOL = id("registry_sync/mod_protocol");
	record ModProtocol(String prioritizedId, Collection<ModProtocolDef> protocols) implements CustomPayload {
		@Override
		public void write(PacketByteBuf buf) {
			buf.writeString(prioritizedId);
			buf.writeCollection(protocols, ModProtocolDef::write);
		}

		@Override
		public Identifier id() {
			return MOD_PROTOCOL;
		}
	}

	private static Identifier id(String path) {
		return new Identifier("qsl", path);
	}
}
