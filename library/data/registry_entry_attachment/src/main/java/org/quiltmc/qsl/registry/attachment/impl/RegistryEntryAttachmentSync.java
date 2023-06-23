/*
 * Copyright 2021 The Quilt Project
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.fabricmc.api.EnvType;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;

@ApiStatus.Internal
public final class RegistryEntryAttachmentSync {
	/**
	 * Indicates the packet version.
	 * <p>
	 * This value should be updated whenever packet formats are changed.
	 */
	private static final byte PACKET_VERSION = 1;

	private RegistryEntryAttachmentSync() {
	}

	public static final Identifier PACKET_ID = id("sync");

	private record NamespaceValuePair(String namespace, Set<AttachmentEntry> entries) {
	}

	private record CacheEntry(Identifier registryId, Set<NamespaceValuePair> namespacesToValues) {
	}

	private record AttachmentEntry(String path, boolean isTag, NbtElement value) {
		public void write(PacketByteBuf buf) {
			buf.writeString(this.path);
			buf.writeBoolean(this.isTag);

			NbtCompound compound = new NbtCompound();
			compound.put("value", this.value);
			buf.writeNbt(compound);
		}

		public static AttachmentEntry read(PacketByteBuf buf) {
			String path = buf.readString();
			boolean isTag = buf.readBoolean();
			NbtElement value = buf.readNbt().get("value");

			return new AttachmentEntry(path, isTag, value);
		}
	}

	public static final Map<Identifier, CacheEntry> ENCODED_VALUES_CACHE = new Object2ReferenceOpenHashMap<>();

	public static void register() {
		ServerPlayConnectionEvents.JOIN.register(RegistryEntryAttachmentSync::syncAttachmentsToPlayer);
	}

	@ClientOnly
	public static void registerClient() {
		ClientPlayNetworking.registerGlobalReceiver(PACKET_ID, RegistryEntryAttachmentSync::receiveSyncPacket);
	}

	public static List<PacketByteBuf> createSyncPackets() {
		fillEncodedValuesCache();
		var bufs = new ArrayList<PacketByteBuf>();

		for (var entry : ENCODED_VALUES_CACHE.entrySet()) {
			for (var valueMap : entry.getValue().namespacesToValues()) {
				var buf = PacketByteBufs.create();
				buf.writeByte(PACKET_VERSION);
				buf.writeIdentifier(entry.getValue().registryId());
				buf.writeIdentifier(entry.getKey());
				buf.writeString(valueMap.namespace());
				buf.writeInt(valueMap.entries().size());
				for (AttachmentEntry attachmentEntry : valueMap.entries()) {
					attachmentEntry.write(buf);
				}

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
			if (isPlayerLocal(player)) continue;

			for (var buf : createSyncPackets()) {
				ServerPlayNetworking.send(player, PACKET_ID, buf);
			}
		}
	}

	private static boolean isPlayerLocal(ServerPlayerEntity player) {
		if (MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
			return player.getUuid().equals(MinecraftClient.getInstance().getSession().getPlayerUuid());
		}

		return false;
	}

	public static void clearEncodedValuesCache() {
		ENCODED_VALUES_CACHE.clear();
	}

	@SuppressWarnings("unchecked")
	private static void fillEncodedValuesCache() {
		if (!ENCODED_VALUES_CACHE.isEmpty()) {
			return;
		}

		for (var registryEntry : Registries.REGISTRY.getEntries()) {
			var registry = (Registry<Object>) registryEntry.getValue();
			var dataHolder = RegistryEntryAttachmentHolder.getData(registry);

			for (var attachmentEntry : RegistryEntryAttachmentHolder.getAttachmentEntries(registry)) {
				var attachment = (RegistryEntryAttachment<Object, Object>) attachmentEntry.getValue();
				if (attachment.side() != RegistryEntryAttachment.Side.BOTH) {
					continue;
				}

				// Namespace, Attachment
				var encoded = new HashMap<String, Set<AttachmentEntry>>();
				Map<Object, Object> entryValues = dataHolder.valueTable.rowMap().get(attachmentEntry.getValue());
				if (entryValues != null) {
					for (var valueEntry : entryValues.entrySet()) {
						var entryId = registry.getId(valueEntry.getKey());
						if (entryId == null) {
							throw new IllegalStateException("Foreign object in data holder of attachment %s: %s"
									.formatted(attachment.id(), valueEntry.getKey()));
						}

						encoded.computeIfAbsent(entryId.getNamespace(), id -> new HashSet<>()).add(
								new AttachmentEntry(entryId.getPath(), false, attachment.codec()
										.encodeStart(NbtOps.INSTANCE, valueEntry.getValue())
										.getOrThrow(false, msg -> {
											throw new IllegalStateException("Failed to encode value for attachment %s of registry entry %s: %s"
													.formatted(attachment.id(), entryId, msg));
										})
								)
						);
					}
				}

				Map<TagKey<Object>, Object> entryTagValues = dataHolder.valueTagTable.rowMap().get(attachmentEntry.getValue());
				if (entryTagValues != null) {
					for (var valueEntry : entryTagValues.entrySet()) {
						encoded.computeIfAbsent(valueEntry.getKey().id().getNamespace(), id -> new HashSet<>()).add(
								new AttachmentEntry(valueEntry.getKey().id().getPath(), true, attachment.codec()
										.encodeStart(NbtOps.INSTANCE, valueEntry.getValue())
										.getOrThrow(false, msg -> {
											throw new IllegalStateException("Failed to encode value for attachment tag %s of registry %s: %s"
													.formatted(attachment.id(), valueEntry.getKey().id(), msg));
										})));
					}
				}

				var valueMaps = new HashSet<NamespaceValuePair>();
				for (var namespaceEntry : encoded.entrySet()) {
					valueMaps.add(new NamespaceValuePair(namespaceEntry.getKey(), namespaceEntry.getValue()));
				}

				ENCODED_VALUES_CACHE.put(attachment.id(), new CacheEntry(attachment.registry().getKey().getValue(), valueMaps));
			}
		}
	}

	private static void syncAttachmentsToPlayer(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
		if (isPlayerLocal(handler.getPlayer())) return;

		for (var buf : RegistryEntryAttachmentSync.createSyncPackets()) {
			sender.sendPacket(RegistryEntryAttachmentSync.PACKET_ID, buf);
		}
	}

	@ClientOnly
	@SuppressWarnings("unchecked")
	private static void receiveSyncPacket(MinecraftClient client, ClientPlayNetworkHandler handler, PacketByteBuf buf, PacketSender responseSender) {
		var packetVersion = buf.readByte();
		if (packetVersion != PACKET_VERSION) {
			throw new UnsupportedOperationException("Unable to read RegistryEntryAttachmentSync packet. Please install the same version of QSL as the server you play on");
		}

		var registryId = buf.readIdentifier();
		var attachmentId = buf.readIdentifier();
		var namespace = buf.readString();

		var size = buf.readInt();
		var attachments = new HashSet<AttachmentEntry>();

		while (size > 0) {
			attachments.add(AttachmentEntry.read(buf));
			size--;
		}

		client.execute(() -> {
			var registry = (Registry<Object>) Registries.REGISTRY.get(registryId);
			if (registry == null) {
				throw new IllegalStateException("Unknown registry %s".formatted(registryId));
			}

			var attachment = (RegistryEntryAttachment<Object, Object>) RegistryEntryAttachmentHolder.getAttachment(registry, attachmentId);
			if (attachment == null) {
				throw new IllegalStateException("Unknown attachment %s for registry %s".formatted(attachmentId, registryId));
			}

			var holder = RegistryEntryAttachmentHolder.getData(registry);
			holder.valueTable.row(attachment).clear();
			holder.valueTagTable.row(attachment).clear();

			for (AttachmentEntry attachmentEntry : attachments) {
				var entryId = new Identifier(namespace, attachmentEntry.path);

				var registryObject = registry.get(entryId);
				if (registryObject == null) {
					throw new IllegalStateException("Foreign ID %s".formatted(entryId));
				}

				var parsedValue = attachment.codec()
						.parse(NbtOps.INSTANCE, attachmentEntry.value)
						.getOrThrow(false, msg -> {
							throw new IllegalStateException("Failed to decode value for attachment %s of registry entry %s: %s"
									.formatted(attachment.id(), entryId, msg));
						});

				if (attachmentEntry.isTag) {
					holder.putValue(attachment, TagKey.of(registry.getKey(), entryId), parsedValue);
				} else {
					holder.putValue(attachment, registryObject, parsedValue);
				}
			}
		});
	}
}
