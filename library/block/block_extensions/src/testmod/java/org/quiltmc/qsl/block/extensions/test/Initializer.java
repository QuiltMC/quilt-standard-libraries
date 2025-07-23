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

import java.util.function.Function;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.GlassBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.extensions.api.QuiltBlockSettings;

public final class Initializer implements ModInitializer {
	public static final String NAMESPACE = "quilt_block_extensions_testmod";

	public static final Block BLOCK = registerBlock(
			"test_block",
			QuiltBlockSettings.copyOf(Blocks.GLASS),
			settings -> new GlassBlock(settings
				.luminance(15)
				.pistonBehavior(PistonBehavior.PUSH_ONLY)
			)
	);

	public static final Block BLOCK2 = registerBlock(
			"test_block2",
			QuiltBlockSettings.copyOf(Blocks.VINE),
			settings -> new VineBlock(settings.ticksRandomly(false))
	);

	public static Identifier createId(String path) {
		return Identifier.of(NAMESPACE, path);
	}

	public static RegistryKey<Block> createBlockKey(String path) {
		return RegistryKey.of(RegistryKeys.BLOCK, createId(path));
	}

	private static <B extends Block, S extends AbstractBlock.Settings> B registerBlock(
			String path, S baseSettings, Function<S, B> factory
	) {
		RegistryKey<Block> key = createBlockKey(path);
		baseSettings.key(key);
		return Registry.register(Registries.BLOCK, key, factory.apply(baseSettings));
	}

	@Override
	public void onInitialize(ModContainer mod) { }
}
