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
import java.util.function.BiFunction;
import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import com.google.common.collect.ImmutableMap;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.block.ComposterBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
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
	public static final Supplier<Map<Item, Integer>> INITIAL_FUEL_ITEM_MAP = Suppliers.memoize(() -> ImmutableMap.copyOf(AbstractFurnaceBlockEntity.createFuelTimeMap()));
	public static final Map<TagKey<Item>, Integer> INITIAL_FUEL_TAG_MAP = new Reference2ObjectOpenHashMap<>();
	public static final Map<Item, Integer> FUEL_MAP = new Reference2ObjectOpenHashMap<>();

	public static final Map<ItemConvertible, Float> INITIAL_COMPOST_CHANCE = ImmutableMap.copyOf(ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE);

	public static final TagKey<Item> FUEL_FILTERS = TagKey.of(RegistryKeys.ITEM, new Identifier("quilt", "fuel_filters"));

	private static boolean collectInitialTags = false;

	@Override
	public void onInitialize(ModContainer mod) {
		collectInitialTags = true;
		INITIAL_FUEL_ITEM_MAP.get().forEach(ItemContentRegistries.FUEL_TIMES::put);
		INITIAL_FUEL_TAG_MAP.forEach(ItemContentRegistries.FUEL_TIMES::put);
		collectInitialTags = false;

		INITIAL_COMPOST_CHANCE.forEach((item, f) -> ItemContentRegistries.COMPOST_CHANCES.put(item.asItem(), f));

		ResourceLoaderEvents.END_DATA_PACK_RELOAD.register(context -> {
			if (context.error().isPresent()) return;

			FUEL_MAP.clear();
			// Fill the fuel map with all entries on the FUEL_TIMES registry attachment but filter using the #quilt:fuel_filters tag
			for (var entry : ItemContentRegistries.FUEL_TIMES) {
				if (!entry.entry().getBuiltInRegistryHolder().isIn(FUEL_FILTERS)) {
					FUEL_MAP.put(entry.entry(), entry.value());
				}
			}

			ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.clear();
			setMapFromAttachment(ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE::put, ItemContentRegistries.COMPOST_CHANCES);
		});
	}

	private static <T, V> void setMapFromAttachment(BiFunction<T, V, ?> map, RegistryEntryAttachment<T, V> attachment) {
		attachment.forEach(entry -> map.apply(entry.entry(), entry.value()));
	}

	public static boolean shouldCollectInitialTags() {
		return collectInitialTags;
	}
}
