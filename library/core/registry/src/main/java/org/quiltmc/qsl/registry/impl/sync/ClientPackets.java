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

import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

/**
 * Identifiers of packets sent by server.
 */
@ApiStatus.Internal
public final class ClientPackets {
	/**
	 * Response for {@link ServerPackets.Handshake#ID}. Selects the registry sync version to be used from the server's supported options.
	 *
	 * <pre><code>
	 * {
	 *     Supported Version: VarInt
	 * }
	 * </code></pre>
	 */
	public record Handshake(int version) implements CustomPayload {
		public static final Identifier ID = ClientPackets.id("registry_sync/handshake");

		public Handshake(PacketByteBuf buf) {
			this(
					buf.readVarInt()
			);
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeVarInt(this.version);
		}

		@Override
		public Identifier id() {
			return ID;
		}
	}

	/**
	 * Sent after registry sync failure before client disconnect.
	 *
	 * <pre><code>
	 * {
	 *     Registry: Identifier
	 * }
	 * </code></pre>
	 */
	public record SyncFailed(Identifier registry) implements CustomPayload {
		public static final Identifier ID = ClientPackets.id("registry_sync/sync_failed");

		public SyncFailed(PacketByteBuf buf) {
			this(buf.readIdentifier());
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeIdentifier(this.registry);
		}

		@Override
		public Identifier id() {
			return ID;
		}
	}

	/**
	 * Sent after synchronization of selected registry.
	 * Contains list of (optional) unknown entries.
	 * It's sent after successful validation of {@link ServerPackets.RegistryApply#ID}
	 * Requires protocol version 3 or higher.
	 *
	 * <pre><code>
	 * {
	 *     Registry: Identifier
	 *     Raw Registry Ids: IntList
	 * }
	 * </code></pre>
	 */
	public record UnknownEntry(Identifier registry, IntList rawIds) implements CustomPayload {
		public static final Identifier ID = ClientPackets.id("registry_sync/unknown_entry");

		public UnknownEntry(PacketByteBuf buf) {
			this(buf.readIdentifier(), buf.readIntList());
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeIdentifier(this.registry);
			buf.writeIntList(this.rawIds);
		}

		@Override
		public Identifier id() {
			return ID;
		}
	}

	/**
	 * Sent after receiving Mod Protocol request packet from server.
	 * Returns all latest supported by client version of requested Mod Protocols see {@link ServerPackets.ModProtocol#ID}
	 *
	 * <pre><code>
	 * {
	 *   Count of Entries: VarInt
	 *   [
	 *     Id: String
	 *     Highest Supported Version: VarInt
	 *   ]
	 * }
	 * </code></pre>
	 */
	public record ModProtocol(Object2IntOpenHashMap<String> protocols) implements CustomPayload {
		public static final Identifier ID = ClientPackets.id("registry_sync/mod_protocol");

		public ModProtocol(PacketByteBuf buf) {
			this(read(buf));
		}

		private static Object2IntOpenHashMap<String> read(PacketByteBuf buf) {
			Object2IntOpenHashMap<String> protocols = new Object2IntOpenHashMap<>();

			int count = buf.readVarInt();

			while (count-- > 0) {
				protocols.put(buf.readString(), buf.readVarInt());
			}

			return protocols;
		}

		@Override
		public void write(PacketByteBuf buf) {
			buf.writeVarInt(this.protocols.size());
			for (var entry : this.protocols.object2IntEntrySet()) {
				buf.writeString(entry.getKey());
				buf.writeVarInt(entry.getIntValue());
			}
		}

		@Override
		public Identifier id() {
			return ID;
		}
	}

	/**
	 * Ends registry sync. No data
	 */
	public record End() implements CustomPayload {
		public static final Identifier ID = ClientPackets.id("registry_sync/end");

		public End(PacketByteBuf buf) {
			this();
		}

		@Override
		public void write(PacketByteBuf buf) {
		}

		@Override
		public Identifier id() {
			return ID;
		}
	}

	private static Identifier id(String path) {
		return new Identifier("qsl", path);
	}
}
