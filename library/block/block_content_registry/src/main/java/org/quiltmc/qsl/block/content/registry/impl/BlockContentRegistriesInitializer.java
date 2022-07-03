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

package org.quiltmc.qsl.block.content.registry.impl;

import java.util.Map;
import java.util.function.BiFunction;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.content.registry.api.BlockContentRegistries;
import org.quiltmc.qsl.block.content.registry.api.FlammableBlockEntry;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.ShovelItem;

public class BlockContentRegistriesInitializer implements ModInitializer {
	private static final Map<Block, BlockState> INITIAL_PATH_STATES = ImmutableMap.copyOf(ShovelItem.PATH_STATES);
	private static final Map<Block, Block> INITIAL_STRIPPED_BLOCKS = ImmutableMap.copyOf(AxeItem.STRIPPED_BLOCKS);

	public static final BiMap<Block, Block> INITIAL_OXIDATION_BLOCKS = HashBiMap.create();
	public static final BiMap<Block, Block> OXIDATION_INCREASE_BLOCKS = HashBiMap.create();
	public static final BiMap<Block, Block> OXIDATION_DECREASE_BLOCKS = HashBiMap.create();

	public static final BiMap<Block, Block> INITIAL_WAXED_BLOCKS = HashBiMap.create();
	public static final BiMap<Block, Block> WAXED_UNWAXED_BLOCKS = HashBiMap.create();
	public static final BiMap<Block, Block> UNWAXED_WAXED_BLOCKS = HashBiMap.create();

	private static final Map<Block, FlammableBlockEntry> INITIAL_FLAMMABLE_BLOCKS;

	static {
		var builder = ImmutableMap.<Block, FlammableBlockEntry>builder();
		FireBlock fireBlock = ((FireBlock) Blocks.FIRE);
		fireBlock.spreadChances.keySet().forEach(block -> builder.put(block, new FlammableBlockEntry(fireBlock.burnChances.getInt(block), fireBlock.spreadChances.getInt(block))));
		INITIAL_FLAMMABLE_BLOCKS = builder.build();
	}

	@Override
	public void onInitialize(ModContainer mod) {
		// Force load the maps
		Oxidizable.OXIDATION_LEVEL_INCREASES.get();
		HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get();

		resetMaps();
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, error) -> resetMaps());
	}

	private static void resetMaps() {
		ShovelItem.PATH_STATES.clear();
		addMapToAttachment(INITIAL_PATH_STATES, BlockContentRegistries.FLATTENABLE_BLOCK);
		setMapFromAttachment(ShovelItem.PATH_STATES::put, BlockContentRegistries.FLATTENABLE_BLOCK);

		AxeItem.STRIPPED_BLOCKS.clear();
		addMapToAttachment(INITIAL_STRIPPED_BLOCKS, BlockContentRegistries.STRIPPABLE_BLOCK);
		setMapFromAttachment(AxeItem.STRIPPED_BLOCKS::put, BlockContentRegistries.STRIPPABLE_BLOCK);

		OXIDATION_INCREASE_BLOCKS.clear();
		OXIDATION_DECREASE_BLOCKS.clear();
		addMapToAttachment(INITIAL_OXIDATION_BLOCKS, BlockContentRegistries.OXIDIZABLE_BLOCK);
		setMapFromAttachment(OXIDATION_INCREASE_BLOCKS::put, BlockContentRegistries.OXIDIZABLE_BLOCK);
		OXIDATION_DECREASE_BLOCKS.putAll(OXIDATION_INCREASE_BLOCKS.inverse());

		UNWAXED_WAXED_BLOCKS.clear();
		WAXED_UNWAXED_BLOCKS.clear();
		addMapToAttachment(INITIAL_WAXED_BLOCKS, BlockContentRegistries.WAXABLE_BLOCK);
		setMapFromAttachment(UNWAXED_WAXED_BLOCKS::put, BlockContentRegistries.WAXABLE_BLOCK);
		WAXED_UNWAXED_BLOCKS.putAll(UNWAXED_WAXED_BLOCKS.inverse());

		FireBlock fireBlock = ((FireBlock) Blocks.FIRE);
		fireBlock.burnChances.clear();
		fireBlock.spreadChances.clear();
		addMapToAttachment(INITIAL_FLAMMABLE_BLOCKS, BlockContentRegistries.FLAMMABLE_BLOCK);
		BlockContentRegistries.FLAMMABLE_BLOCK.registry().stream().forEach(entry -> BlockContentRegistries.FLAMMABLE_BLOCK.getValue(entry).ifPresent(v -> {
			fireBlock.burnChances.put(entry, v.burn());
			fireBlock.spreadChances.put(entry, v.spread());
		}));
	}

	private static <T, V> void setMapFromAttachment(BiFunction<T, V, ?> map, RegistryEntryAttachment<T, V> attachment) {
		attachment.registry().stream().forEach(entry -> attachment.getValue(entry).ifPresent(v -> map.apply(entry, v)));
	}

	private static <T, V> void addMapToAttachment(Map<T, V> map, RegistryEntryAttachment<T, V> attachment) {
		map.forEach(attachment::put);
	}
}
