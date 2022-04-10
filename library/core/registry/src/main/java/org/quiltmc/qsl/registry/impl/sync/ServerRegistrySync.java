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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.registry.api.sync.RegistryFlag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@ApiStatus.Internal
public final class ServerRegistrySync {
	public static void sendSyncPackets(ClientConnection connection, ServerPlayerEntity player) {
		for (var registry : Registry.REGISTRIES) {
			if (registry instanceof SynchronizedRegistry<?> synchronizedRegistry
				&& synchronizedRegistry.quilt$requiresSyncing() && synchronizedRegistry.quilt$getContentStatus() != SynchronizedRegistry.Status.VANILLA) {
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

				connection.send(ServerPlayNetworking.createS2CPacket(ServerPackets.APPLY, PacketByteBufs.empty()));
			}
		}
	}

	private static <T extends Registry<?>> void sendStartPacket(ClientConnection connection, T registry) {
		var buf = PacketByteBufs.create();

		buf.writeIdentifier(((Registry<T>) Registry.REGISTRIES).getId(registry));
		buf.writeVarInt(registry.size());
		var flag = ((SynchronizedRegistry<T>) registry).quilt$getRegistryFlag();
		if (((SynchronizedRegistry<T>) registry).quilt$getContentStatus() == SynchronizedRegistry.Status.OPTIONAL) {
			flag |= (0x1 << RegistryFlag.OPTIONAL.ordinal());
		}
		buf.writeByte(flag);

		connection.send(ServerPlayNetworking.createS2CPacket(ServerPackets.START, buf));
	}

	private static void sendDataPacket(ClientConnection connection, Map<String, ArrayList<SynchronizedRegistry.SyncEntry>> packetData) {
		var buf = PacketByteBufs.create();
		buf.writeVarInt(packetData.size());
		for (var key : packetData.keySet()) {
			var list = packetData.get(key);

			buf.writeString(key);
			buf.writeVarInt(list.size());

			for (var entry : list) {
				buf.writeString(entry.path());
				buf.writeVarInt(entry.rawId());
				buf.writeByte(entry.flags());
			}
		}

		connection.send(ServerPlayNetworking.createS2CPacket(ServerPackets.DATA, buf));

		packetData.clear();
	}

}
