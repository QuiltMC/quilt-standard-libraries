/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.registry.impl.sync.client;

import com.google.common.base.Preconditions;
import io.netty.buffer.ByteBuf;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.registry.impl.sync.ServerFabricRegistrySync;
import org.quiltmc.qsl.registry.impl.sync.SynchronizedRegistry;

import java.util.*;
import java.util.zip.Deflater;

/**
 * Legacy (Fabric) registry sync.
 * <p>
 * Direct port from Fabric API
 */
@ApiStatus.Internal
@Deprecated
@Environment(EnvType.CLIENT)
public class ClientFabricRegistrySync {
	@Nullable
	private static PacketByteBuf combinedBuf;

	@Nullable
	private static Map<Identifier, Object2IntMap<Identifier>> syncedRegistryMap;

	private static boolean isPacketFinished = false;

	public static void registerHandlers() {
		ClientPlayNetworking.registerGlobalReceiver(ServerFabricRegistrySync.ID, ClientFabricRegistrySync::handlePacket);
	}

	private static void handlePacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender sender) {
		receiveSlicedPacket(buf);

		if (isPacketFinished) {
			applyRegistry(handler);
		}
	}

	private static void receiveSlicedPacket(PacketByteBuf slicedBuf) {
		Preconditions.checkState(!isPacketFinished);

		if (combinedBuf == null) {
			combinedBuf = PacketByteBufs.create();
		}

		if (slicedBuf.readableBytes() != 0) {
			combinedBuf.writeBytes(slicedBuf);
			return;
		}

		isPacketFinished = true;

		computeBufSize(combinedBuf);
		syncedRegistryMap = new LinkedHashMap<>();
		int regNamespaceGroupAmount = combinedBuf.readVarInt();

		for (int i = 0; i < regNamespaceGroupAmount; i++) {
			String regNamespace = combinedBuf.readString();
			int regNamespaceGroupLength = combinedBuf.readVarInt();

			for (int j = 0; j < regNamespaceGroupLength; j++) {
				String regPath = combinedBuf.readString();
				Object2IntMap<Identifier> idMap = new Object2IntLinkedOpenHashMap<>();
				int idNamespaceGroupAmount = combinedBuf.readVarInt();

				int lastBulkLastRawId = 0;

				for (int k = 0; k < idNamespaceGroupAmount; k++) {
					String idNamespace = combinedBuf.readString();
					int rawIdBulkAmount = combinedBuf.readVarInt();

					for (int l = 0; l < rawIdBulkAmount; l++) {
						int bulkRawIdStartDiff = combinedBuf.readVarInt();
						int bulkSize = combinedBuf.readVarInt();

						int currentRawId = (lastBulkLastRawId + bulkRawIdStartDiff) - 1;

						for (int m = 0; m < bulkSize; m++) {
							currentRawId++;
							String idPath = combinedBuf.readString();
							idMap.put(new Identifier(idNamespace, idPath), currentRawId);
						}

						lastBulkLastRawId = currentRawId;
					}
				}

				syncedRegistryMap.put(new Identifier(regNamespace, regPath), idMap);
			}
		}

		combinedBuf.release();
		combinedBuf = null;
	}

	private static void computeBufSize(PacketByteBuf buf) {
		final byte[] deflateBuffer = new byte[8192];
		ByteBuf byteBuf = buf.copy();
		Deflater deflater = new Deflater();

		int i = byteBuf.readableBytes();
		PacketByteBuf deflatedBuf = PacketByteBufs.create();

		if (i < 256) {
			deflatedBuf.writeVarInt(0);
			deflatedBuf.writeBytes(byteBuf);
		} else {
			byte[] bs = new byte[i];
			byteBuf.readBytes(bs);
			deflatedBuf.writeVarInt(bs.length);
			deflater.setInput(bs, 0, i);
			deflater.finish();

			while (!deflater.finished()) {
				int j = deflater.deflate(deflateBuffer);
				deflatedBuf.writeBytes(deflateBuffer, 0, j);
			}

			deflater.reset();
		}
	}

	@SuppressWarnings("unchecked")
	private static void applyRegistry(ClientPlayNetworkHandler handler) {
		Preconditions.checkState(isPacketFinished);
		Map<Identifier, Object2IntMap<Identifier>> map = syncedRegistryMap;
		isPacketFinished = false;
		syncedRegistryMap = null;

		for (var entry : map.entrySet()) {
			var registry = Registry.REGISTRIES.get(entry.getKey());

			if (registry instanceof SynchronizedRegistry currentRegistry) {
				var syncMap = new HashMap<String, Collection<SynchronizedRegistry.SyncEntry>>();

				for (var entry2 : entry.getValue().object2IntEntrySet()) {
					syncMap.computeIfAbsent(entry2.getKey().getNamespace(), (x) -> new ArrayList<>())
							.add(new SynchronizedRegistry.SyncEntry(entry2.getKey().getPath(), entry2.getIntValue(), (byte) 0));
				}

				var missingEntries = currentRegistry.quilt$applySyncMap(syncMap);

				if (ClientRegistrySync.checkMissing(handler, registry.getKey().getValue(), missingEntries)) {
					break;
				}
			}
		}

		ClientRegistrySync.rebuildEverything(MinecraftClient.getInstance());
	}
}
