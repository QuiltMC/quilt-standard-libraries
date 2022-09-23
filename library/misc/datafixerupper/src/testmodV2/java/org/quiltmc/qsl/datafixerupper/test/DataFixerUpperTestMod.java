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

package org.quiltmc.qsl.datafixerupper.test;

import java.io.IOException;
import java.nio.file.Files;

import com.mojang.datafixers.schemas.Schema;
import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.block.Block;
import net.minecraft.block.Material;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.datafixer.schema.IdentifierNormalizingSchema;
import net.minecraft.item.Item;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.datafixerupper.api.QuiltDataFixerBuilder;
import org.quiltmc.qsl.datafixerupper.api.QuiltDataFixes;
import org.quiltmc.qsl.datafixerupper.api.SimpleFixes;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;

public final class DataFixerUpperTestMod implements ModInitializer, ServerLifecycleEvents.Ready {
	private static final String NAMESPACE = "quilt_datafixerupper_testmod";
	private static final int DATA_VERSION = 1;

	private static final Logger LOGGER = LogUtils.getLogger();

	private static final Item ITEM = Registry.register(
			Registry.ITEM, new Identifier(NAMESPACE, "new_item"), new Item(new Item.Settings()));
	private static final Block BLOCK = Registry.register(
			Registry.BLOCK, new Identifier(NAMESPACE, "old_block"), new Block(Block.Settings.of(Material.STONE)));

	@Override
	public void onInitialize(ModContainer mod) {
		if (!Files.exists(QuiltLoader.getGameDir().resolve("dfu-testmod-v1.txt"))) {
			throw new IllegalStateException("DataFixer testmod v1 must be run before v2!");
		}

		QuiltDataFixerBuilder builder = new QuiltDataFixerBuilder(DATA_VERSION);
		builder.addSchema(0, QuiltDataFixes.BASE_SCHEMA);
		Schema schemaV1 = builder.addSchema(1, IdentifierNormalizingSchema::new);
		SimpleFixes.addItemRenameFix(builder, "Rename old_item to new_item",
				new Identifier(NAMESPACE, "old_item"), new Identifier(NAMESPACE, "new_item"), schemaV1);

		QuiltDataFixes.buildAndRegisterFixer(mod, builder);
	}

	@Override
	public void readyServer(MinecraftServer server) {
		var world = server.getWorld(World.OVERWORLD);
		if (world == null) {
			throw new IllegalStateException("NO OVERWORLD?!");
		}

		var chest = world.getBlockEntity(BlockPos.ORIGIN, BlockEntityType.CHEST)
				.orElseThrow(() -> new IllegalStateException("no chest block entity?"));

		if (chest.getStack(0).getItem() != ITEM) {
			throw new IllegalStateException("TEST FAILED - Item was not upgraded!");
		}

		LOGGER.info("[v2] TEST SUCCEEDED - Item was upgraded!");

		world.setBlockState(BlockPos.ORIGIN.add(1, 0, 0), BLOCK.getDefaultState());

		try (var writer = Files.newBufferedWriter(QuiltLoader.getGameDir().resolve("dfu-testmod-v2.txt"))) {
			writer.write("DataFixerUpper testmod v2 was run!");
		} catch (IOException e) {
			throw new RuntimeException("Failed to write marker file", e);
		}

		LOGGER.info("[v2] Prepared for v3 test!");
	}
}
