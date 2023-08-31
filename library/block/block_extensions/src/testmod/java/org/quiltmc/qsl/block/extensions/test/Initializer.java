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

package org.quiltmc.qsl.block.extensions.test;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

public final class Initializer implements ModInitializer {
	public static final Block BLOCK = Registry.register(Registries.BLOCK,
			new Identifier("quilt_block_extensions_testmod", "test_block"),
			new GlassBlock(QuiltBlockSettings.copyOf(Blocks.GLASS)
					.luminance(15)
					.pistonBehavior(PistonBehavior.PUSH_ONLY)));

	public static final Block BLOCK2 = Registry.register(Registries.BLOCK,
			new Identifier("quilt_block_extensions_testmod", "test_block2"),
			new VineBlock(QuiltBlockSettings.copyOf(Blocks.VINE).ticksRandomly(false)));

	@Override
	public void onInitialize(ModContainer mod) {}
}
