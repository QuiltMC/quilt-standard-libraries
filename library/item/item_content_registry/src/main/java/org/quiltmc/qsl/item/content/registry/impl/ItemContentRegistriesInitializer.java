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

package org.quiltmc.qsl.item.content.registry.impl;

import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.block.ComposterBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.HolderSet;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.content.registry.api.ItemContentRegistries;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;

@ApiStatus.Internal
public class ItemContentRegistriesInitializer implements ModInitializer {
	public static final Map<Item, Integer> INITIAL_FUEL_ITEM_MAP = new Object2IntOpenHashMap<>();
	public static final Map<TagKey<Item>, Integer> INITIAL_FUEL_TAG_MAP = new Reference2ObjectOpenHashMap<>();

	public static final Map<ItemConvertible, Float> INITIAL_COMPOST_CHANCE = ImmutableMap.copyOf(ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE);

	public static final TagKey<Item> FUEL_FILTERS = TagKey.of(RegistryKeys.ITEM, Identifier.of("quilt", "fuel_filters"));

	private static boolean collectInitialFuels = false;

	@Override
	public void onInitialize(ModContainer mod) {
		INITIAL_COMPOST_CHANCE.forEach((item, f) -> ItemContentRegistries.COMPOST_CHANCES.put(item.asItem(), f));

		ResourceLoaderEvents.END_DATA_PACK_RELOAD.register(context -> {
			if (context.error().isPresent()) {
				return;
			}

			ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.clear();
			setMapFromAttachment(ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE::put, ItemContentRegistries.COMPOST_CHANCES);
		});
	}

	private static <T, V> void setMapFromAttachment(BiFunction<T, V, ?> map, RegistryEntryAttachment<T, V> attachment) {
		attachment.forEach(entry -> map.apply(entry.entry(), entry.value()));
	}

	public static void startInitialFuelCollection() {
		collectInitialFuels = true;

		INITIAL_FUEL_ITEM_MAP.clear();
		INITIAL_FUEL_TAG_MAP.clear();
	}

	public static boolean shouldCollectInitialFuels() {
		return collectInitialFuels;
	}

	public static void endInitialFuelCollection() {
		collectInitialFuels = false;

		// Since this is run after datapacks are first loaded, we should only add fields that aren't already included
		INITIAL_FUEL_ITEM_MAP.forEach((item, fuelTime) -> {
			if (!ItemContentRegistries.FUEL_TIMES.keySet().contains(item)) {
				ItemContentRegistries.FUEL_TIMES.put(item, fuelTime);
			}
		});

		INITIAL_FUEL_TAG_MAP.forEach((tag, fuelTime) -> {
			if (!ItemContentRegistries.FUEL_TIMES.tagKeySet().contains(tag)) {
				ItemContentRegistries.FUEL_TIMES.put(tag, fuelTime);
			}
		});

		Optional<HolderSet.NamedSet<Item>> tag = Registries.ITEM.getTag(FUEL_FILTERS);
		tag.ifPresent(filters -> filters.forEach(filter ->
				ItemContentRegistries.FUEL_TIMES.remove(filter.getValue())
		));
	}
}
