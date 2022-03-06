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

import net.fabricmc.loader.api.ModContainer;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;
import org.quiltmc.qsl.block.extensions.api.QuiltMaterialBuilder;

public final class BlockExtensionsTestMod implements ModInitializer {
	public static final String ID = "quilt_block_extensions_testmod";

	public static final Material MATERIAL = QuiltMaterialBuilder.copyOf(Material.GLASS, MapColor.DARK_GREEN)
			.pistonBehavior(PistonBehavior.PUSH_ONLY)
			.build();

	public static final Block BLOCK = Registry.register(Registry.BLOCK,
			new Identifier(ID, "test_block"),
			new GlassBlock(QuiltBlockSettings.copyOf(Blocks.GLASS)
					.material(MATERIAL)
					.luminance(15)));

	@Override
	public void onInitialize(ModContainer mod) {
	}
}
