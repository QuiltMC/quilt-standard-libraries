/*
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.qsl.registry.attachment.impl;

import static org.quiltmc.qsl.registry.attachment.impl.Initializer.id;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;

@ApiStatus.Internal
public final class RegistryEntryAttachmentSync {
	private RegistryEntryAttachmentSync() {
	}

	public static final Identifier PACKET_ID = id("sync");

	public record CacheEntry(Identifier registryId,
	                         Set<Pair<String, NbtCompound>> valueMaps) {
	}

	public static final Map<Identifier, CacheEntry> ENCODED_VALUES_CACHE = new Object2ReferenceOpenHashMap<>();

	public static void register() {
		ServerPlayConnectionEvents.JOIN.register(RegistryEntryAttachmentSync::syncAttachmentsToPlayer);
	}

	@Environment(EnvType.CLIENT)
	public static void registerClient() {
		ClientPlayNetworking.registerGlobalReceiver(PACKET_ID, RegistryEntryAttachmentSync::receiveSyncPacket);
	}

	public static List<PacketByteBuf> createSyncPackets() {
		fillEncodedValuesCache();
		var bufs = new ArrayList<PacketByteBuf>();

		for (var entry : ENCODED_VALUES_CACHE.entrySet()) {
			for (var valueMap : entry.getValue().valueMaps()) {
				var buf = PacketByteBufs.create();
				buf.writeIdentifier(entry.getValue().registryId());
				buf.writeIdentifier(entry.getKey());
				buf.writeString(valueMap.getLeft());
				buf.writeNbt(valueMap.getRight());
				bufs.add(buf);
			}
		}

		return bufs;
	}

	public static void syncAttachmentsToAllPlayers() {
		var server = Initializer.getServer();
		if (server == null) {
			return;
		}

		for (var player : server.getPlayerManager().getPlayerList()) {
			for (var buf : createSyncPackets()) {
				ServerPlayNetworking.send(player, PACKET_ID, buf);
			}
		}
	}

	public static void clearEncodedValuesCache() {
		ENCODED_VALUES_CACHE.clear();
	}

	@SuppressWarnings("unchecked")
	private static void fillEncodedValuesCache() {
		if (!ENCODED_VALUES_CACHE.isEmpty()) {
			return;
		}

		for (var registryEntry : Registry.REGISTRIES.getEntries()) {
			var registry = (Registry<Object>) registryEntry.getValue();
			var dataHolder = RegistryEntryAttachmentHolder.getData(registry);

			for (var attachmentEntry : RegistryEntryAttachmentHolder.getAttachmentEntries(registry)) {
				var attachment = (RegistryEntryAttachment<Object, Object>) attachmentEntry.getValue();
				if (attachment.side() != RegistryEntryAttachment.Side.BOTH) {
					continue;
				}

				@SuppressWarnings("UnstableApiUsage")
				Table<String, String, NbtElement> myTable = Tables.newCustomTable(new Object2ReferenceOpenHashMap<>(), Object2ReferenceOpenHashMap::new);
				Map<Object, Object> entryValues = dataHolder.valueTable.rowMap().get(attachmentEntry.getValue());
				if (entryValues != null) {
					for (var valueEntry : entryValues.entrySet()) {
						var entryId = registry.getId(valueEntry.getKey());
						if (entryId == null) {
							throw new IllegalStateException("Foreign object in data holder of attachment %s: %s"
									.formatted(attachment.id(), valueEntry.getKey()));
						}

						myTable.put(entryId.getNamespace(), entryId.getPath(), attachment.codec()
								.encodeStart(NbtOps.INSTANCE, valueEntry.getValue())
								.getOrThrow(false, msg -> {
									throw new IllegalStateException("Failed to encode value for attachment %s of registry entry %s: %s"
											.formatted(attachment.id(), entryId, msg));
								}));
					}
				}

				Set<Pair<String, NbtCompound>> valueMaps = new HashSet<>();
				for (var tableEntry : myTable.rowMap().entrySet()) {
					/*var valueMap = new NbtCompound();
					for (var valueEntry : tableEntry.getValue().entrySet()) {
						valueMap.put(valueEntry.getKey(), valueEntry.getValue());
					}*/
					// this is probably a horrible idea lmao
					var valueMap = new NbtCompound(Map.copyOf(tableEntry.getValue())) {
					};
					valueMaps.add(new Pair<>(tableEntry.getKey(), valueMap));
				}

				ENCODED_VALUES_CACHE.put(attachment.id(), new CacheEntry(attachment.registry().getKey().getValue(), valueMaps));
			}
		}
	}

	private static void syncAttachmentsToPlayer(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
		for (var buf : RegistryEntryAttachmentSync.createSyncPackets()) {
			sender.sendPacket(RegistryEntryAttachmentSync.PACKET_ID, buf);
		}
	}

	@Environment(EnvType.CLIENT)
	@SuppressWarnings("unchecked")
	private static void receiveSyncPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		var registryId = buf.readIdentifier();
		var attachmentId = buf.readIdentifier();
		var namespace = buf.readString();
		var valueMap = buf.readNbt();

		client.execute(() -> {
			var registry = (Registry<Object>) Registry.REGISTRIES.get(registryId);
			if (registry == null) {
				throw new IllegalStateException("Unknown registry %s".formatted(registryId));
			}

			var attachment = (RegistryEntryAttachment<Object, Object>) RegistryEntryAttachmentHolder.getAttachment(registry, attachmentId);
			if (attachment == null) {
				throw new IllegalStateException("Unknown attachment %s for registry %s".formatted(attachmentId, registryId));
			}

			var holder = RegistryEntryAttachmentHolder.getData(registry);
			holder.valueTable.row(attachment).clear();
			for (var entryKey : valueMap.getKeys()) {
				var entryId = new Identifier(namespace, entryKey);

				var registryObject = registry.get(entryId);
				if (registryObject == null) {
					throw new IllegalStateException("Foreign ID %s".formatted(entryId));
				}

				var parsedValue = attachment.codec()
						.parse(NbtOps.INSTANCE, valueMap.get(entryKey))
						.getOrThrow(false, msg -> {
							throw new IllegalStateException("Failed to decode value for attachment %s of registry entry %s: %s"
									.formatted(attachment.id(), entryId, msg));
						});

				holder.putValue(attachment, registryObject, parsedValue);
			}
		});
		// TODO send "OK" response packet?
	}
}
