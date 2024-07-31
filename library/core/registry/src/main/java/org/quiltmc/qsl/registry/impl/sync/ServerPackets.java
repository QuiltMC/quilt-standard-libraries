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
import java.util.HashMap;
import java.util.Map;

import it.unimi.dsi.fastutil.ints.Int2ObjectArrayMap;
import it.unimi.dsi.fastutil.ints.IntList;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.impl.sync.mod_protocol.ModProtocolDef;
import org.quiltmc.qsl.registry.impl.sync.registry.RegistryFlag;
import org.quiltmc.qsl.registry.impl.sync.registry.SynchronizedRegistry;

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
	public record Handshake(IntList supportedVersions) implements CustomPayload {
		public static final CustomPayload.Id<Handshake> ID = ServerPackets.id("registry_sync/handshake");
		public static final PacketCodec<PacketByteBuf, Handshake> CODEC = CustomPayload.create(Handshake::write, Handshake::new);

		public Handshake(PacketByteBuf buf) {
			this(buf.readIntList());
		}

		private void write(PacketByteBuf buf) {
			buf.writeIntList(this.supportedVersions);
		}

		@Override
		public CustomPayload.Id<Handshake> getId() {
			return ID;
		}
	}

	/**
	 * Ends registry sync. No data
	 */
	public record End() implements CustomPayload {
		public static final CustomPayload.Id<End> ID = ServerPackets.id("registry_sync/end");
		public static final PacketCodec<PacketByteBuf, End> CODEC = CustomPayload.create(End::write, End::new);

		public End(PacketByteBuf buf) {
			this();
		}

		private void write(PacketByteBuf buf) {
		}

		@Override
		public CustomPayload.Id<End> getId() {
			return ID;
		}
	}

	/**
	 * Sets current registry for next {@link RegistryData#ID} and {@link RegistryRestore#ID} packets.
	 *
	 * <pre><code>
	 * {
	 *   Registry identifier: Identifier
	 *   Count of entries: VarInt
	 *   Flags: byte
	 * }
	 * </code></pre>
	 */
	public record RegistryStart<T extends Registry<?>>(Identifier registry, int size, byte flags) implements CustomPayload {
		public static final CustomPayload.Id<RegistryStart<?>> ID = ServerPackets.id("registry_sync/registry_start");
		public static final PacketCodec<PacketByteBuf, RegistryStart<?>> CODEC = CustomPayload.create(RegistryStart::write, RegistryStart::new);

		@SuppressWarnings("unchecked")
		public RegistryStart(T registry) {
			this((((Registry<T>) Registries.ROOT).getId(registry)), registry.size(), getFlags((SynchronizedRegistry<T>) registry));
		}

		public RegistryStart(PacketByteBuf buf) {
			this(buf.readIdentifier(), buf.readVarInt(), buf.readByte());
		}

		private static <T extends Registry<?>> byte getFlags(SynchronizedRegistry<T> registry) {
			var flags = registry.quilt$getRegistryFlag();
			if (registry.quilt$getContentStatus() == SynchronizedRegistry.Status.OPTIONAL) {
				flags |= (byte) (0x1 << RegistryFlag.OPTIONAL.ordinal());
			}

			return flags;
		}

		private void write(PacketByteBuf buf) {
			buf.writeIdentifier(this.registry);
			buf.writeVarInt(this.size);
			buf.writeByte(this.flags);
		}

		@Override
		public CustomPayload.Id<RegistryStart<?>> getId() {
			return ID;
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
	public record RegistryData(Map<String, ArrayList<SynchronizedRegistry.SyncEntry>> packetData) implements CustomPayload {
		public static final CustomPayload.Id<RegistryData> ID = ServerPackets.id("registry_sync/registry_data");
		public static final PacketCodec<PacketByteBuf, RegistryData> CODEC = CustomPayload.create(RegistryData::write, RegistryData::new);

		public RegistryData(PacketByteBuf buf) {
			this(read(buf));
		}

		private static Map<String, ArrayList<SynchronizedRegistry.SyncEntry>> read(PacketByteBuf buf) {
			var data = new HashMap<String, ArrayList<SynchronizedRegistry.SyncEntry>>();
			int countNamespace = buf.readVarInt();
			while (countNamespace-- > 0) {
				var namespace = buf.readString();
				int countLocal = buf.readVarInt();

				while (countLocal-- > 0) {
					var path = buf.readString();
					int id = buf.readVarInt();
					byte flags = buf.readByte();

					data.computeIfAbsent(namespace, n -> new ArrayList<>()).add(new SynchronizedRegistry.SyncEntry(path, id, flags));
				}
			}

			return data;
		}

		private void write(PacketByteBuf buf) {
			buf.writeVarInt(this.packetData.size());
			for (var key : this.packetData.keySet()) {
				var list = this.packetData.get(key);

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
		public CustomPayload.Id<RegistryData> getId() {
			return ID;
		}
	}

	/**
	 * Applies changes to current registry, doesn't have any data.
	 */
	public record RegistryApply() implements CustomPayload {
		public static final CustomPayload.Id<RegistryApply> ID = ServerPackets.id("registry_sync/registry_apply");
		public static final PacketCodec<PacketByteBuf, RegistryApply> CODEC = CustomPayload.create(RegistryApply::write, RegistryApply::new);

		public RegistryApply(PacketByteBuf buf) {
			this();
		}

		private void write(PacketByteBuf buf) {
		}

		@Override
		public CustomPayload.Id<RegistryApply> getId() {
			return ID;
		}
	}

	/**
	 * This packet requests client to validate ids of {@link net.minecraft.block.Block#STATE_IDS} or {@link net.minecraft.fluid.Fluid#STATE_IDS} to prevent id mismatch.
	 * It might not send all states in single packet! It needs to be verified as is (aka, ids matter, count doesn't).
	 *
	 * <pre><code>
	 * {
	 *   Count of Entries: VarInt
	 *   [
	 *     Block/Fluid Id: VarInt
	 *     Count of Entries: VarInt
	 *     [
	 *       Block/Fluid State Id: VarInt
	 *     ]
	 *   ]
	 * }
	 * </code></pre>
	 */
	public record ValidateStates(StateType type, Int2ObjectArrayMap<IntList> packetData) implements CustomPayload {
		public static final PacketCodec<PacketByteBuf, ValidateStates> CODEC_BLOCK = CustomPayload.create(ValidateStates::write, ValidateStates::newBlock);
		public static final PacketCodec<PacketByteBuf, ValidateStates> CODEC_FLUID = CustomPayload.create(ValidateStates::write, ValidateStates::newFluid);
		public ValidateStates(StateType type, PacketByteBuf buf) {
			this(type, read(buf));
		}

		private static Int2ObjectArrayMap<IntList> read(PacketByteBuf buf) {
			Int2ObjectArrayMap<IntList> data = new Int2ObjectArrayMap<>();
			var count = buf.readVarInt();

			while (count-- > 0) {
				var blockId = buf.readVarInt();
				var states = buf.readIntList();
				data.put(blockId, states);
			}

			return data;
		}

		private void write(PacketByteBuf buf) {
			buf.writeVarInt(this.packetData.size());
			for (var entry : this.packetData.int2ObjectEntrySet()) {
				buf.writeVarInt(entry.getIntKey());
				buf.writeIntList(entry.getValue());
			}
		}

		@Override
		public CustomPayload.Id<ValidateStates> getId() {
			return this.type.packetId;
		}

		public static ValidateStates newBlock(PacketByteBuf buf) {
			return new ValidateStates(StateType.BLOCK, buf);
		}

		public static ValidateStates newFluid(PacketByteBuf buf) {
			return new ValidateStates(StateType.FLUID, buf);
		}

		public enum StateType {
			BLOCK(ServerPackets.id("registry_sync/validate/block_states")), FLUID(ServerPackets.id("registry_sync/validate/fluid_states"));

			private final CustomPayload.Id<ValidateStates> packetId;

			StateType(CustomPayload.Id<ValidateStates> packetId) {
				this.packetId = packetId;
			}

			public CustomPayload.Id<ValidateStates> packetId() {
				return this.packetId;
			}
		}
	}

	/**
	 * Applies changes to current registry, doesn't have any data.
	 */
	public record RegistryRestore() implements CustomPayload {
		public static final CustomPayload.Id<RegistryRestore> ID = ServerPackets.id("registry_sync/registry_restore");
		public static final PacketCodec<PacketByteBuf, RegistryRestore> CODEC = CustomPayload.create(RegistryRestore::write, RegistryRestore::new);

		public RegistryRestore(PacketByteBuf buf) {
			this();
		}

		private void write(PacketByteBuf buf) {
		}

		@Override
		public CustomPayload.Id<RegistryRestore> getId() {
			return ID;
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
	public record ErrorStyle(Text errorHeader, Text errorFooter, boolean showError) implements CustomPayload {
		public static final CustomPayload.Id<ErrorStyle> ID = ServerPackets.id("registry_sync/error_style");
		public static final PacketCodec<PacketByteBuf, ErrorStyle> CODEC = CustomPayload.create(ErrorStyle::write, ErrorStyle::new);

		public ErrorStyle(PacketByteBuf buf) {
			this(Text.SerializationUtil.fromLenientJson(buf.readString(PacketByteBuf.MAX_TEXT_LENGTH), DynamicRegistryManager.EMPTY),
					Text.SerializationUtil.fromLenientJson(buf.readString(PacketByteBuf.MAX_TEXT_LENGTH), DynamicRegistryManager.EMPTY),
					buf.readBoolean());
		}

		private void write(PacketByteBuf buf) {
			buf.writeString(Text.SerializationUtil.toJson(this.errorHeader, DynamicRegistryManager.EMPTY));
			buf.writeString(Text.SerializationUtil.toJson(this.errorFooter, DynamicRegistryManager.EMPTY));
			buf.writeBoolean(this.showError);
		}

		@Override
		public CustomPayload.Id<ErrorStyle> getId() {
			return ID;
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
	public record ModProtocol(String prioritizedId, Collection<ModProtocolDef> protocols) implements CustomPayload {
		public static final CustomPayload.Id<ModProtocol> ID = ServerPackets.id("registry_sync/mod_protocol");
		public static final PacketCodec<PacketByteBuf, ModProtocol> CODEC = CustomPayload.create(ModProtocol::write, ModProtocol::new);

		public ModProtocol(PacketByteBuf buf) {
			this(buf.readString(), buf.readList(ModProtocolDef::read));
		}

		private void write(PacketByteBuf buf) {
			buf.writeString(this.prioritizedId);
			buf.writeCollection(this.protocols, ModProtocolDef::write);
		}

		@Override
		public CustomPayload.Id<ModProtocol> getId() {
			return ID;
		}
	}

	private static <T extends CustomPayload> CustomPayload.Id<T> id(String path) {
		return new CustomPayload.Id<>(Identifier.of("qsl", path));
	}
}
