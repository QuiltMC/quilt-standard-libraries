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

package org.quiltmc.qsl.item.content.registry.impl;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;

import net.minecraft.block.ComposterBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.item.Item;

public class ItemContentRegistriesInitializer implements ModInitializer {
	public static final Map<Item, Integer> FUEL_MAP = new Reference2ObjectOpenHashMap<>();

	@Override
	public void onInitialize(ModContainer mod) {
		AbstractFurnaceBlockEntity.createFuelTimeMap();

		ResourceLoaderEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, error) -> {
			FUEL_MAP.clear();
			AbstractFurnaceBlockEntity.createFuelTimeMap();

			ComposterBlock.ITEM_TO_LEVEL_INCREASE_CHANCE.clear();
			ComposterBlock.registerDefaultCompostableItems();
		});
	}
}
