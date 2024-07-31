/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.registry.impl.sync.server;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.server.ServerPlayNetworking;
import org.quiltmc.qsl.registry.impl.sync.registry.RegistryFlag;
import org.quiltmc.qsl.registry.impl.sync.registry.SynchronizedRegistry;

/**
 * Legacy (Fabric) registry sync. Direct port from Fabric API.
 */
@Deprecated
@ApiStatus.Internal
public class ServerFabricRegistrySync {
	private static final int MAX_PAYLOAD_SIZE = 1048576;

	public static void sendSyncPackets(Consumer<Packet<?>> sender) {
		var registryMap = createRegistryMap();

		PacketByteBuf buf = PacketByteBufs.create();

		// Group registry ids with same namespace.
		Map<String, List<Identifier>> regNamespaceGroups = registryMap.keySet().stream()
				.collect(Collectors.groupingBy(Identifier::getNamespace));

		buf.writeVarInt(regNamespaceGroups.size());

		regNamespaceGroups.forEach((regNamespace, regIds) -> {
			buf.writeString(optimizeNamespace(regNamespace));
			buf.writeVarInt(regIds.size());

			for (Identifier regId : regIds) {
				buf.writeString(regId.getPath());

				Object2IntMap<Identifier> idMap = registryMap.get(regId);

				// Sort object ids by its namespace. We use linked map here to keep the original namespace ordering.
				Map<String, List<Object2IntMap.Entry<Identifier>>> idNamespaceGroups = idMap.object2IntEntrySet().stream()
						.collect(Collectors.groupingBy(e -> e.getKey().getNamespace(), LinkedHashMap::new, Collectors.toCollection(ArrayList::new)));

				buf.writeVarInt(idNamespaceGroups.size());

				int lastBulkLastRawId = 0;

				for (Map.Entry<String, List<Object2IntMap.Entry<Identifier>>> idNamespaceEntry : idNamespaceGroups.entrySet()) {
					// Make sure the ids are sorted by its raw id.
					List<Object2IntMap.Entry<Identifier>> idPairs = idNamespaceEntry.getValue();
					idPairs.sort(Comparator.comparingInt(Object2IntMap.Entry::getIntValue));

					// Group consecutive raw ids together.
					List<List<Object2IntMap.Entry<Identifier>>> bulks = new ArrayList<>();

					Iterator<Object2IntMap.Entry<Identifier>> idPairIter = idPairs.iterator();
					List<Object2IntMap.Entry<Identifier>> currentBulk = new ArrayList<>();
					Object2IntMap.Entry<Identifier> currentPair = idPairIter.next();
					currentBulk.add(currentPair);

					while (idPairIter.hasNext()) {
						currentPair = idPairIter.next();

						if (currentBulk.getLast().getIntValue() + 1 != currentPair.getIntValue()) {
							bulks.add(currentBulk);
							currentBulk = new ArrayList<>();
						}

						currentBulk.add(currentPair);
					}

					bulks.add(currentBulk);

					buf.writeString(optimizeNamespace(idNamespaceEntry.getKey()));
					buf.writeVarInt(bulks.size());

					for (List<Object2IntMap.Entry<Identifier>> bulk : bulks) {
						int firstRawId = bulk.getFirst().getIntValue();
						int bulkRawIdStartDiff = firstRawId - lastBulkLastRawId;

						buf.writeVarInt(bulkRawIdStartDiff);
						buf.writeVarInt(bulk.size());

						for (Object2IntMap.Entry<Identifier> idPair : bulk) {
							buf.writeString(idPair.getKey().getPath());

							lastBulkLastRawId = idPair.getIntValue();
						}
					}
				}
			}
		});

		// Split the packet to multiple MAX_PAYLOAD_SIZEd buffers.
		int readableBytes = buf.readableBytes();
		int sliceIndex = 0;

		while (sliceIndex < readableBytes) {
			int sliceSize = Math.min(readableBytes - sliceIndex, MAX_PAYLOAD_SIZE);
			PacketByteBuf slicedBuf = PacketByteBufs.slice(buf, sliceIndex, sliceSize);
			sendPacket(sender, slicedBuf);
			sliceIndex += sliceSize;
		}

		// Send an empty buffer to mark the end of the split.
		sendPacket(sender, PacketByteBufs.empty());
	}

	@SuppressWarnings("rawtypes")
	private static Map<Identifier, Object2IntMap<Identifier>> createRegistryMap() {
		var map = new HashMap<Identifier, Object2IntMap<Identifier>>();

		for (var registry : Registries.ROOT) {
			if (registry instanceof SynchronizedRegistry<?> synchronizedRegistry
					&& synchronizedRegistry.quilt$requiresSyncing() && synchronizedRegistry.quilt$getContentStatus() != SynchronizedRegistry.Status.VANILLA) {
				var idMap = new Object2IntOpenHashMap<Identifier>();
				var syncMap = synchronizedRegistry.quilt$getSyncMap();

				for (var entry : syncMap.entrySet()) {
					for (var entry2 : entry.getValue()) {
						if (!RegistryFlag.isOptional(entry2.flags()) && !RegistryFlag.isSkipped(entry2.flags())) {
							idMap.put(Identifier.of(entry.getKey(), entry2.path()), entry2.rawId());
						}
					}
				}

				map.put(((Registry) registry).getKey().getValue(), idMap);
			}
		}

		return map;
	}

	private static void sendPacket(Consumer<Packet<?>> sender, PacketByteBuf buf) {
		sender.accept(ServerPlayNetworking.createS2CPacket(new Payload(buf.array())));
	}

	private static String optimizeNamespace(String namespace) {
		return namespace.equals(Identifier.DEFAULT_NAMESPACE) ? "" : namespace;
	}

	public record Payload(byte[] data) implements CustomPayload {
		public static CustomPayload.Id<Payload> ID = new Id<>(Identifier.of("fabric", "registry/sync/direct"));
		public static PacketCodec<PacketByteBuf, Payload> CODEC = CustomPayload.create(Payload::write, Payload::new);

		Payload(PacketByteBuf buf) {
			this(readAllBytes(buf));
		}

		private void write(PacketByteBuf buf) {
			buf.writeBytes(this.data);
		}

		private static byte[] readAllBytes(PacketByteBuf buf) {
			byte[] bytes = new byte[buf.readableBytes()];
			buf.readBytes(bytes);
			return bytes;
		}

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}

	public static class SyncCompletePayload implements CustomPayload {
		public static final SyncCompletePayload INSTANCE = new SyncCompletePayload();
		public static final CustomPayload.Id<?> ID = new CustomPayload.Id<>(Identifier.of("fabric", "registry/sync/complete"));
		public static final PacketCodec<PacketByteBuf, SyncCompletePayload> CODEC = PacketCodec.unit(INSTANCE);

		private SyncCompletePayload() { }

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}
}
