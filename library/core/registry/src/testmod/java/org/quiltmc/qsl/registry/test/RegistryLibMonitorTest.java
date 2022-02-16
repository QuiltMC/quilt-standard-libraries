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

package org.quiltmc.qsl.registry.test;

import java.util.HashSet;

import net.fabricmc.loader.api.ModContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.registry.api.event.RegistryMonitor;

public class RegistryLibMonitorTest implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("Quilt Registry Lib Monitor Test");

	private static final Identifier TEST_BLOCK_A_ID = new Identifier("quilt_registry_test_monitors", "test_block_a");
	private static final Identifier TEST_BLOCK_B_ID = new Identifier("quilt_registry_test_monitors", "test_block_b");

	@Override
	public void onInitialize(ModContainer mod) {
		Block blockA = Registry.register(Registry.BLOCK, TEST_BLOCK_A_ID, new Block(AbstractBlock.Settings.of(Material.STONE, MapColor.BLACK)));

		var monitor = RegistryMonitor.create(Registry.BLOCK)
				.filter(context -> context.id().getNamespace().equals("quilt_registry_test_monitors"));

		var allList = new HashSet<Block>();
		var upcomingList = new HashSet<Block>();

		monitor.forAll(context -> {
			LOGGER.info("[forAll event]: Block {} id={} raw={} had its registration monitored in registry {}",
					context.value(), context.id(), context.rawId(), context.registry());
			allList.add(context.value());
		});
		monitor.forUpcoming(context -> {
			LOGGER.info("[forUpcoming event]: Block {} id={} raw={} had its registration monitored in registry {}",
					context.value(), context.id(), context.rawId(), context.registry());
			upcomingList.add(context.value());
		});

		Block blockB = Registry.register(Registry.BLOCK, TEST_BLOCK_B_ID, new Block(AbstractBlock.Settings.of(Material.STONE, MapColor.BLACK)));

		if (!allList.contains(blockA) || !allList.contains(blockB)) {
			throw new AssertionError("Entries " + allList + " found by RegistryMonitor via forAll were not as expected");
		}

		if (upcomingList.contains(blockA) || !upcomingList.contains(blockB)) {
			throw new AssertionError("Entries " + upcomingList + " found by RegistryMonitor via forUpcoming were not as expected");
		}
	}
}
