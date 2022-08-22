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

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;

public final class DataFixerUpperTestMod implements ModInitializer, ServerLifecycleEvents.Ready {
	private static final String NAMESPACE = "quilt_datafixerupper_testmod";

	private static final Logger LOGGER = LogUtils.getLogger();

	private static final Item ITEM = Registry.register(
			Registry.ITEM, new Identifier(NAMESPACE, "old_item"), new Item(new Item.Settings()));

	@Override
	public void onInitialize(ModContainer mod) {}

	@Override
	public void readyServer(MinecraftServer server) {
		var world = server.getWorld(World.OVERWORLD);
		if (world == null) {
			throw new IllegalStateException("NO OVERWORLD?!");
		}

		world.setBlockState(BlockPos.ORIGIN, Blocks.CHEST.getDefaultState());
		world.addBlockEntity(new ChestBlockEntity(BlockPos.ORIGIN, Blocks.CHEST.getDefaultState()));

		var chest = world.getBlockEntity(BlockPos.ORIGIN, BlockEntityType.CHEST)
				.orElseThrow(() -> new IllegalStateException("no chest block entity?"));

		chest.setStack(0, new ItemStack(ITEM, 1));

		try (var writer = Files.newBufferedWriter(QuiltLoader.getGameDir().resolve("dfu-testmod-v1.txt"))) {
			writer.write("DataFixerUpper testmod v1 was run!");
		} catch (IOException e) {
			throw new RuntimeException("Failed to write marker file", e);
		}

		LOGGER.info("[v1] Prepared for v2 test!");
	}
}
