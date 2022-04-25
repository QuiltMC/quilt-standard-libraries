/*
 * Copyright 2022 QuiltMC
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

import net.minecraft.network.ClientConnection;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public final class ServerRegistrySync {
	private static final Logger LOGGER = LoggerFactory.getLogger("quilt_registry_sync");

	public static void registerHandlers() {
		ServerPlayNetworking.registerGlobalReceiver(ClientPackets.HANDSHAKE, ServerRegistrySync::handleHelloPacket);
		ServerPlayNetworking.registerGlobalReceiver(ClientPackets.SYNC_FAILED, ServerRegistrySync::handleSyncFailedPacket);
	}

	// This is currently unused, as it requires "preplay" networking api to be useful in any way
	// Why can't it use Minecraft's Login stage you might ask? That's simple, it would limit compatibility with
	// any proxy software like Velocity (this is issue with forge for example).
	// Ideally we should introduce new stage/api for it by creating custom ServerPlayPacketListener
	// that just handles all of custom logic/emulation of login stage, which is fairly possible
	// to do while keeping compatibility with vanilla clients (as long as other mods on server allow it).
	// It's thanks to ping packets send by server and tcp/Minecraft preserving packet order!
	// But for now they will be registered, but just ignored
	private static void handleHelloPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {}

	private static void handleSyncFailedPacket(MinecraftServer server, ServerPlayerEntity player, ServerPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		LOGGER.info("Disconnecting {} due to sync failure of {} registry", player.getGameProfile().getName(), buf.readIdentifier());
	}

	public static void sendSyncPackets(ClientConnection connection, ServerPlayerEntity player) {
		boolean sentHello = false;
		for (var registry : Registry.REGISTRIES) {
			if (registry instanceof SynchronizedRegistry<?> synchronizedRegistry
				&& synchronizedRegistry.quilt$requiresSyncing() && synchronizedRegistry.quilt$getContentStatus() != SynchronizedRegistry.Status.VANILLA) {
				if (!sentHello) {
					sentHello = true;
					sendHelloPacket(connection);
				}

				var map = synchronizedRegistry.quilt$getSyncMap();

				var packetData = new HashMap<String, ArrayList<SynchronizedRegistry.SyncEntry>>();

				sendStartPacket(connection, registry);
				int dataLength = 0;

				for (var key : map.keySet()) {
					dataLength += key.length();
					var collection = map.get(key);
					for (var entry : collection) {
						packetData.computeIfAbsent(key, (k) -> new ArrayList<>()).add(entry);
						dataLength += entry.path().length() + 4 + 1;

						if (dataLength > 524288) {
							sendDataPacket(connection, packetData);
							dataLength = 0;
						}
					}

					if (packetData.size() > 0) {
						sendDataPacket(connection, packetData);
					}
				}

				connection.send(ServerPlayNetworking.createS2CPacket(ServerPackets.REGISTRY_APPLY, PacketByteBufs.empty()));
			}
		}

		if (sentHello) {
			connection.send(ServerPlayNetworking.createS2CPacket(ServerPackets.END, PacketByteBufs.empty()));
		}
	}

	private static void sendHelloPacket(ClientConnection connection) {
		var buf = PacketByteBufs.create();

		buf.writeVarInt(ServerPackets.SUPPORTED_VERSIONS.size());

		for (var version : ServerPackets.SUPPORTED_VERSIONS) {
			buf.writeVarInt(version);
		}

		connection.send(ServerPlayNetworking.createS2CPacket(ServerPackets.HANDSHAKE, buf));
	}

	private static <T extends Registry<?>> void sendStartPacket(ClientConnection connection, T registry) {
		var buf = PacketByteBufs.create();

		// Registry id
		buf.writeIdentifier(((Registry<T>) Registry.REGISTRIES).getId(registry));

		// Number of entries
		buf.writeVarInt(registry.size());

		// Registry flags
		var flag = ((SynchronizedRegistry<T>) registry).quilt$getRegistryFlag();
		if (((SynchronizedRegistry<T>) registry).quilt$getContentStatus() == SynchronizedRegistry.Status.OPTIONAL) {
			flag |= (0x1 << RegistryFlag.OPTIONAL.ordinal());
		}
		buf.writeByte(flag);

		connection.send(ServerPlayNetworking.createS2CPacket(ServerPackets.REGISTRY_START, buf));
	}

	private static void sendDataPacket(ClientConnection connection, Map<String, ArrayList<SynchronizedRegistry.SyncEntry>> packetData) {
		var buf = PacketByteBufs.create();

		// Number of namespaces
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

		connection.send(ServerPlayNetworking.createS2CPacket(ServerPackets.REGISTRY_DATA, buf));

		packetData.clear();
	}
}
