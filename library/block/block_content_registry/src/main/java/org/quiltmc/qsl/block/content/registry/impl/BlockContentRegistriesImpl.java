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
import java.util.stream.Collectors;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableMap;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.block.Oxidizable;
import net.minecraft.item.AxeItem;
import net.minecraft.item.HoneycombItem;
import net.minecraft.item.ShovelItem;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.content.registry.api.BlockContentRegistries;
import org.quiltmc.qsl.block.content.registry.api.FlammableBlockEntry;
import org.quiltmc.qsl.block.content.registry.api.ReversibleBlockEntry;
import org.quiltmc.qsl.block.content.registry.api.enchanting.ConstantBooster;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;

@ApiStatus.Internal
public class BlockContentRegistriesImpl implements ModInitializer {
	private static final Map<Block, BlockState> INITIAL_PATH_STATES = ImmutableMap.copyOf(ShovelItem.PATH_STATES);
	private static final Map<Block, Block> INITIAL_STRIPPED_BLOCKS = ImmutableMap.copyOf(AxeItem.STRIPPED_BLOCKS);

	public static final BiMap<Block, Block> INITIAL_OXIDATION_BLOCKS = HashBiMap.create();
	public static final BiMap<Block, Block> OXIDATION_INCREASE_BLOCKS = HashBiMap.create();
	public static final BiMap<Block, Block> OXIDATION_DECREASE_BLOCKS = HashBiMap.create();

	public static final BiMap<Block, Block> INITIAL_WAXED_BLOCKS = HashBiMap.create();
	public static final BiMap<Block, Block> WAXED_UNWAXED_BLOCKS = HashBiMap.create();
	public static final BiMap<Block, Block> UNWAXED_WAXED_BLOCKS = HashBiMap.create();

	@Override
	public void onInitialize(ModContainer mod) {
		// Fill the initial flammable blocks map
		var builder = ImmutableMap.<Block, FlammableBlockEntry>builder();
		FireBlock fireBlock = ((FireBlock) Blocks.FIRE);
		fireBlock.spreadChances.keySet().forEach(block ->
				builder.put(block, new FlammableBlockEntry(fireBlock.burnChances.getInt(block), fireBlock.spreadChances.getInt(block)))
		);
		var initialFlammableBlocks = builder.build();

		// Force load the maps
		Oxidizable.OXIDATION_LEVEL_INCREASES.get();
		HoneycombItem.UNWAXED_TO_WAXED_BLOCKS.get();

		addMapToAttachment(INITIAL_PATH_STATES, BlockContentRegistries.FLATTENABLE);
		addMapToAttachment(INITIAL_STRIPPED_BLOCKS, BlockContentRegistries.STRIPPABLE);
		addMapToAttachment(INITIAL_OXIDATION_BLOCKS.entrySet().stream().collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> new ReversibleBlockEntry(entry.getValue(), true)
		)), BlockContentRegistries.OXIDIZABLE);
		addMapToAttachment(INITIAL_WAXED_BLOCKS.entrySet().stream().collect(Collectors.toMap(
				Map.Entry::getKey,
				entry -> new ReversibleBlockEntry(entry.getValue(), true)
		)), BlockContentRegistries.WAXABLE);
		addMapToAttachment(initialFlammableBlocks, BlockContentRegistries.FLAMMABLE);

		BlockContentRegistries.ENCHANTING_BOOSTERS.put(Blocks.BOOKSHELF, new ConstantBooster(1f));

		resetMaps();
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, error) -> resetMaps());
	}

	private static void resetMaps() {
		ShovelItem.PATH_STATES.clear();
		setMapFromAttachment(ShovelItem.PATH_STATES::put, BlockContentRegistries.FLATTENABLE);

		AxeItem.STRIPPED_BLOCKS.clear();
		setMapFromAttachment(AxeItem.STRIPPED_BLOCKS::put, BlockContentRegistries.STRIPPABLE);

		resetSimpleReversibleMap(OXIDATION_INCREASE_BLOCKS, OXIDATION_DECREASE_BLOCKS, BlockContentRegistries.OXIDIZABLE);

		resetSimpleReversibleMap(UNWAXED_WAXED_BLOCKS, WAXED_UNWAXED_BLOCKS, BlockContentRegistries.WAXABLE);

		FireBlock fireBlock = ((FireBlock) Blocks.FIRE);
		fireBlock.burnChances.clear();
		fireBlock.spreadChances.clear();
		BlockContentRegistries.FLAMMABLE.registry().stream().forEach(entry -> BlockContentRegistries.FLAMMABLE.get(entry).ifPresent(v -> {
			fireBlock.burnChances.put(entry, v.burn());
			fireBlock.spreadChances.put(entry, v.spread());
		}));
	}

	private static <T, V> void setMapFromAttachment(BiFunction<T, V, ?> map, RegistryEntryAttachment<T, V> attachment) {
		attachment.forEach(entry -> map.apply(entry.entry(), entry.value()));
	}

	private static <T, V> void addMapToAttachment(Map<T, V> map, RegistryEntryAttachment<T, V> attachment) {
		map.forEach(attachment::put);
	}

	private static void resetSimpleReversibleMap(BiMap<Block, Block> baseWay, BiMap<Block, Block> reversed,
			RegistryEntryAttachment<Block, ReversibleBlockEntry> rea) {
		baseWay.clear();
		reversed.clear();
		setMapFromAttachment((entry, value) -> baseWay.put(entry, value.block()), rea);
		setMapFromAttachment((entry, value) -> value.reversible() ? reversed.put(value.block(), entry) : null, rea);
	}

	/**
	 * Calculates the bookshelf count around a given position.
	 *
	 * @param world the world
	 * @param pos   the position to count around of
	 * @return the bookshelf count around
	 */
	public static float calculateBookshelfCount(World world, BlockPos pos) {
		float count = 0;

		for (BlockPos offset : EnchantingTableBlock.POSSIBLE_BOOKSHELF_LOCATIONS) {
			if (world.isAir(pos.add(offset.getX() / 2, offset.getY(), offset.getZ() / 2))) {
				var blockPos = pos.add(offset);
				var state = world.getBlockState(blockPos);
				var block = state.getBlock();
				count += BlockContentRegistries.ENCHANTING_BOOSTERS.get(block)
						.map(booster -> booster.getEnchantingBoost(world, state, blockPos))
						.orElse(0.0F);
			}
		}

		return count;
	}
}
