package org.quiltmc.qsl.registry.dict.impl;

import java.util.*;

import com.google.common.collect.Table;
import com.google.common.collect.Tables;
import it.unimi.dsi.fastutil.objects.Object2ReferenceOpenHashMap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.Pair;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.registry.dict.api.RegistryDict;

import static org.quiltmc.qsl.registry.dict.impl.Initializer.id;

@ApiStatus.Internal
public final class RegistryDictSync {
	private RegistryDictSync() { }

	public static final Identifier PACKET_ID = id("sync");

	public static final Logger LOGGER = LogManager.getLogger("RegistryDictSync");

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

			for (var dictEntry : RegistryDictHolder.getDictEntries(registry)) {
				var dict = (RegistryDict<Object, Object>) dictEntry.getValue();
				if (dict.side() != RegistryDict.Side.BOTH) {
					continue;
				}

				var dataHolder = RegistryDictHolder.getData(registry);
				var myTable = newTable();
				for (var valueEntry : dataHolder.valueTable.rowMap().get(dictEntry.getValue()).entrySet()) {
					var entryId = registry.getId(valueEntry.getKey());
					if (entryId == null) {
						throw new IllegalStateException("Foreign object in data holder of dictionary %s: %s"
								.formatted(dict.id(), valueEntry.getKey()));
					}

					myTable.put(entryId.getNamespace(), entryId.getPath(), dict.codec()
							.encodeStart(NbtOps.INSTANCE, valueEntry.getKey())
							.getOrThrow(false, msg -> {
								throw new IllegalStateException("Failed to encode value for dictionary %s of registry entry %s: %s"
										.formatted(dict.id(), entryId, msg));
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

				ENCODED_VALUES_CACHE.put(dict.id(), new CacheEntry(dict.registry().getKey().getValue(), valueMaps));
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
