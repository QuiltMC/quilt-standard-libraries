/*
 * Copyright 2021 QuiltMC
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

import java.util.*;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import com.mojang.logging.LogUtils;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;

import static org.quiltmc.qsl.registry.attachment.impl.Initializer.id;

@ApiStatus.Internal
public final class RegistryEntryAttachmentSync {
	private RegistryEntryAttachmentSync() { }

	public static final Identifier PACKET_ID = id("sync");

	public static final Logger LOGGER = LogUtils.getLogger();

	public record CacheEntry(Identifier registryId,
							 Set<Pair<String, NbtCompound>> valueMaps) { }

	public static final Map<Identifier, CacheEntry> ENCODED_VALUES_CACHE = new Object2ReferenceOpenHashMap<>();

	public static void markDirty() {
		ENCODED_VALUES_CACHE.clear();
	}

	@SuppressWarnings("UnstableApiUsage")
	private static Table<String, String, NbtElement> newTable() {
		return Tables.newCustomTable(new Object2ReferenceOpenHashMap<>(), Object2ReferenceOpenHashMap::new);
	}

	@SuppressWarnings("unchecked")
	private static void fillEncodedValuesCache() {
		if (!ENCODED_VALUES_CACHE.isEmpty()) {
			return;
		}

		for (var registryEntry : Registry.REGISTRIES.getEntries()) {
			var registry = (Registry<Object>) registryEntry.getValue();

			for (var attachmentEntry : RegistryEntryAttachmentHolder.getAttachmentEntries(registry)) {
				var attachment = (RegistryEntryAttachment<Object, Object>) attachmentEntry.getValue();
				if (attachment.side() != RegistryEntryAttachment.Side.BOTH) {
					continue;
				}

				var dataHolder = RegistryEntryAttachmentHolder.getData(registry);
				var myTable = newTable();
				for (var valueEntry : dataHolder.valueTable.rowMap().get(attachmentEntry.getValue()).entrySet()) {
					var entryId = registry.getId(valueEntry.getKey());
					if (entryId == null) {
						throw new IllegalStateException("Foreign object in data holder of attachment %s: %s"
								.formatted(attachment.id(), valueEntry.getKey()));
					}

					myTable.put(entryId.getNamespace(), entryId.getPath(), attachment.codec()
							.encodeStart(NbtOps.INSTANCE, valueEntry.getKey())
							.getOrThrow(false, msg -> {
								throw new IllegalStateException("Failed to encode value for attachment %s of registry entry %s: %s"
										.formatted(attachment.id(), entryId, msg));
							}));
				}

				Set<Pair<String, NbtCompound>> valueMaps = new HashSet<>();
				for (var tableEntry : myTable.rowMap().entrySet()) {
					/*var valueMap = new NbtCompound();
					for (var valueEntry : tableEntry.getValue().entrySet()) {
						valueMap.put(valueEntry.getKey(), valueEntry.getValue());
					}*/
					// this is probably a horrible idea lmao
					var valueMap = new NbtCompound(Map.copyOf(tableEntry.getValue())) { };
					valueMaps.add(new Pair<>(tableEntry.getKey(), valueMap));
				}

				ENCODED_VALUES_CACHE.put(attachment.id(), new CacheEntry(attachment.registry().getKey().getValue(), valueMaps));
			}
		}
	}

	public static List<PacketByteBuf> createSyncPackets() {
		List<PacketByteBuf> bufs = new ArrayList<>();
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
}
