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

package org.quiltmc.qsl.block.extensions.test;

import net.minecraft.block.*;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.extensions.api.QuiltBlock;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.block.extensions.api.QuiltMaterialBuilder;

public final class Initializer implements ModInitializer {
	public static final Material MATERIAL = QuiltMaterialBuilder.copyOf(Material.GLASS, MapColor.DARK_GREEN)
			.pistonBehavior(PistonBehavior.PUSH_ONLY)
			.build();

	public static final Block BLOCK = Registry.register(Registry.BLOCK,
			testModId("test_block"),
			new GlassBlock(QuiltBlockSettings.copyOf(Blocks.GLASS)
					.material(MATERIAL)
					.luminance(15)));

	public static Identifier testModId(String path) {
		return new Identifier("quilt_block_extensions_testmod", path);
	}

	public static void registerBlockAndItem(Identifier identifier, Block block) {
		Registry.register(Registry.BLOCK, identifier, block);
		Registry.register(Registry.ITEM, identifier, new BlockItem(block, new Item.Settings().group(ItemGroup.MATERIALS)));

	}

	public static final Block QUILT_BLOCK = new QuiltBlock(QuiltBlockSettings.of(Material.STONE), Blocks.OAK_STAIRS, Blocks.QUARTZ_PILLAR);
	public static final Block QUILT_BLOCK_2 = new QuiltBlock(QuiltBlockSettings.of(Material.STONE), Blocks.OAK_SLAB, Blocks.QUARTZ_PILLAR);
	public static final Block QUILT_BLOCK_3 = new QuiltBlock(QuiltBlockSettings.of(Material.STONE), Blocks.ANDESITE_WALL, Blocks.OAK_SLAB);
	public static final Block QUILT_BLOCK_4 = new QuiltBlock(QuiltBlockSettings.of(Material.STONE), Blocks.POTTED_ALLIUM, Blocks.LANTERN);

	@Override
	public void onInitialize(ModContainer mod) {
		registerBlockAndItem(testModId("oak_stairs__quartz_pillar"), QUILT_BLOCK);
		registerBlockAndItem(testModId("oak_slab__quartz_pillar"), QUILT_BLOCK_2);
		registerBlockAndItem(testModId("andesite_wall__oak_slab"), QUILT_BLOCK_3);
		registerBlockAndItem(testModId("potted_allium__lantern"), QUILT_BLOCK_4);
	}
}
