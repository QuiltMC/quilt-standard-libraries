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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import net.fabricmc.api.EnvType;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.PayloadTypeRegistry;
import org.quiltmc.qsl.networking.api.server.ServerPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.server.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;

@ApiStatus.Internal
public final class RegistryEntryAttachmentSync {
	/**
	 * Indicates the packet version.
	 *
	 * <p>
	 * This value should be updated whenever packet formats are changed.
	 */
	private static final byte PACKET_VERSION = 2;

	private RegistryEntryAttachmentSync() {
	}

	private static final CustomPayload.Id<CachePacket<Object>> PACKET_ID = new CustomPayload.Id<>(id("sync"));

	private record CachePacket<V>(byte version, NamespaceValuePair<V> namespacesToValues) implements CustomPayload {
		CachePacket(NamespaceValuePair<V> namespacesToValues) {
			this(PACKET_VERSION, namespacesToValues);
		}

		@SuppressWarnings("unchecked")
		public static PacketCodec<RegistryByteBuf, CachePacket<Object>> CODEC = PacketCodec
				.tuple(
					PacketCodecs.BYTE,
					CachePacket::version,
					Identifier
						.PACKET_CODEC
						.map(
							id -> (Registry<Object>) Registries.ROOT.get(id),
							registry -> registry.getKey().getValue()
						)
						.<RegistryByteBuf>cast()
						.dispatch(
							RegistryEntryAttachment::registry,
							registry -> Identifier
								.PACKET_CODEC
								.map(
									id -> (RegistryEntryAttachment<Object, Object>) RegistryEntryAttachmentHolder.getAttachment(registry, id),
									RegistryEntryAttachment::id)
								.cast()
						)
						.dispatch(
							entry -> (RegistryEntryAttachment<Object, Object>) entry.registryEntryAttachment,
							NamespaceValuePair::codec
						),
					CachePacket::namespacesToValues,
					CachePacket::new
				);

		@Override
		public Id<? extends CustomPayload> getId() {
			return PACKET_ID;
		}
	}

	private record CacheEntry<V>(Set<NamespaceValuePair<V>> namespacesToValues) {
		Stream<CachePacket<V>> toPayloads() {
			return this.namespacesToValues()
				.stream()
				.map(CachePacket::new);
		}
	}

	private record NamespaceValuePair<V>(String namespace, Set<AttachmentEntry<V>> entries, RegistryEntryAttachment<?, V> registryEntryAttachment) {
		public static <V> PacketCodec<RegistryByteBuf, NamespaceValuePair<V>> codec(RegistryEntryAttachment<?, V> registryEntryAttachment) {
			return PacketCodec.tuple(
				PacketCodecs.STRING,
				NamespaceValuePair::namespace,
				PacketCodecs.collection(HashSet::newHashSet, AttachmentEntry.codec(registryEntryAttachment), Integer.MAX_VALUE),
				NamespaceValuePair::entries,
				(namespace, entries) -> new NamespaceValuePair<>(namespace, entries, registryEntryAttachment)
			);
		}
	}

	private record AttachmentEntry<V>(String path, boolean isTag, V value) {
		public static <V> PacketCodec<RegistryByteBuf, AttachmentEntry<V>> codec(RegistryEntryAttachment<?, V> registryEntryAttachment) {
			return PacketCodec.tuple(
				PacketCodecs.STRING,
				AttachmentEntry::path,
				PacketCodecs.BOOL,
				AttachmentEntry::isTag,
				registryEntryAttachment.packetCodec(),
				AttachmentEntry::value,
				AttachmentEntry::new
			);
		}
	}

	private static final Map<Identifier, CacheEntry<?>> ENCODED_VALUES_CACHE = new Object2ReferenceOpenHashMap<>();

	public static void register() {
		ServerPlayConnectionEvents.JOIN.register(RegistryEntryAttachmentSync::syncAttachmentsToPlayer);
		PayloadTypeRegistry.playS2C().register(PACKET_ID, CachePacket.CODEC);
	}

	@ClientOnly
	public static void registerClient() {
		ClientPlayNetworking.registerGlobalReceiver(PACKET_ID, RegistryEntryAttachmentSync::receiveSyncPacket);
	}

	private static Stream<CachePacket<?>> createSyncPackets() {
		fillEncodedValuesCache();

		return ENCODED_VALUES_CACHE
			.values()
			.stream()
			.flatMap(CacheEntry::toPayloads);
	}

	public static void syncAttachmentsToAllPlayers() {
		var server = Initializer.getServer();

		if (server == null) {
			return;
		}

		for (var player : server.getPlayerManager().getPlayerList()) {
			if (isPlayerLocal(player)) continue;

			createSyncPackets().forEach(cachePacket -> ServerPlayNetworking.send(player, cachePacket));
		}
	}

	private static void syncAttachmentsToPlayer(ServerPlayNetworkHandler handler, PacketSender<CustomPayload> sender, MinecraftServer server) {
		if (isPlayerLocal(handler.getPlayer())) return;

		createSyncPackets().forEach(sender::sendPayload);
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

		for (var registryEntry : Registries.ROOT.getEntries()) {
			var registry = (Registry<Object>) registryEntry.getValue();
			var dataHolder = RegistryEntryAttachmentHolder.getData(registry);

			for (var attachmentEntry : RegistryEntryAttachmentHolder.getAttachmentEntries(registry)) {
				cacheAttachments(attachmentEntry.getValue(), dataHolder, registry);
			}
		}
	}

	@SuppressWarnings("unchecked")
	private static <T, V> void cacheAttachments(RegistryEntryAttachment<T, V> attachment, DataRegistryEntryAttachmentHolder<T> dataHolder, Registry<T> registry) {
		if (attachment.side() != RegistryEntryAttachment.Side.BOTH) {
			return;
		}

		// Namespace, Attachment
		var encoded = new HashMap<String, Set<AttachmentEntry<V>>>();
		Map<T, Object> entryValues = dataHolder.valueTable.rowMap().get(attachment);
		if (entryValues != null) {
			for (var valueEntry : entryValues.entrySet()) {
				var entryId = registry.getId(valueEntry.getKey());
				if (entryId == null) {
					throw new IllegalStateException("Foreign object in data holder of attachment %s: %s"
							.formatted(attachment.id(), valueEntry.getKey()));
				}

				encoded.computeIfAbsent(entryId.getNamespace(), id -> new HashSet<>()).add(
					new AttachmentEntry<>(entryId.getPath(), false, (V) valueEntry.getValue())
				);
			}
		}

		Map<TagKey<T>, Object> entryTagValues = dataHolder.valueTagTable.rowMap().get(attachment);
		if (entryTagValues != null) {
			for (var valueEntry : entryTagValues.entrySet()) {
				encoded.computeIfAbsent(valueEntry.getKey().id().getNamespace(), id -> new HashSet<>()).add(
						new AttachmentEntry<>(valueEntry.getKey().id().getPath(), true, (V) valueEntry.getValue())
				);
			}
		}

		var valueMaps = new HashSet<NamespaceValuePair<V>>();
		for (var namespaceEntry : encoded.entrySet()) {
			valueMaps.add(new NamespaceValuePair<>(namespaceEntry.getKey(), namespaceEntry.getValue(), attachment));
		}

		ENCODED_VALUES_CACHE.put(attachment.id(), new CacheEntry<>(valueMaps));
	}

	@ClientOnly
	private static void receiveSyncPacket(MinecraftClient client, ClientPlayNetworkHandler handler, CachePacket<Object> packet, PacketSender<CustomPayload> responseSender) {
		if (packet.version() != PACKET_VERSION) {
			throw new UnsupportedOperationException("Unable to read RegistryEntryAttachmentSync packet. Please install the same version of QSL as the server you play on");
		}

		client.execute(() -> applyPacket(packet));
	}

	@SuppressWarnings("unchecked")
	private static <V> void applyPacket(CachePacket<V> packet) {
		RegistryEntryAttachment<Object, V> attachment = (RegistryEntryAttachment<Object, V>) packet.namespacesToValues().registryEntryAttachment();
		Registry<Object> registry = attachment.registry();

		var holder = RegistryEntryAttachmentHolder.getData(registry);
		holder.valueTable.row(attachment).clear();
		holder.valueTagTable.row(attachment).clear();

		for (AttachmentEntry<V> attachmentEntry : packet.namespacesToValues().entries()) {
			var entryId = Identifier.of(packet.namespacesToValues().namespace(), attachmentEntry.path);

			var registryObject = registry.get(entryId);
			if (registryObject == null) {
				throw new IllegalStateException("Foreign ID %s".formatted(entryId));
			}

			var parsedValue = attachmentEntry.value();

			if (attachmentEntry.isTag) {
				holder.putValue(attachment, TagKey.of(registry.getKey(), entryId), parsedValue);
			} else {
				holder.putValue(attachment, registryObject, parsedValue);
			}
		}
	}
}
