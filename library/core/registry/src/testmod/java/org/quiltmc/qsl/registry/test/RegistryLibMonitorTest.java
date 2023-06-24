/*
 * Copyright 2022 The Quilt Project
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

import static org.quiltmc.qsl.registry.test.RegistryLibEventsTest.register;

import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.registry.api.event.RegistryMonitor;

public class RegistryLibMonitorTest implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("Quilt Registry Lib Monitor Test");

	private static final Identifier TEST_BLOCK_A_ID = new Identifier("quilt_registry_test_monitors", "test_block_a");
	private static final Identifier TEST_BLOCK_B_ID = new Identifier("quilt_registry_test_monitors", "test_block_b");

	@Override
	public void onInitialize(ModContainer mod) {
		Block blockA = register(TEST_BLOCK_A_ID, new Block(AbstractBlock.Settings.copy(Blocks.STONE).mapColor(MapColor.BLACK)));

		var monitor = RegistryMonitor.create(Registries.BLOCK)
				.filter(context -> context.id().getNamespace().equals("quilt_registry_test_monitors"));

		var allSet = new HashSet<Block>();
		var upcomingSet = new HashSet<Block>();

		monitor.forAll(context -> {
			LOGGER.info("[forAll event]: Block {} id={} raw={} had its registration monitored in registry {}",
					context.value(), context.id(), context.rawId(), context.registry());
			allSet.add(context.value());
		});
		monitor.forUpcoming(context -> {
			LOGGER.info("[forUpcoming event]: Block {} id={} raw={} had its registration monitored in registry {}",
					context.value(), context.id(), context.rawId(), context.registry());
			upcomingSet.add(context.value());
		});

		Block blockB = register(TEST_BLOCK_B_ID, new Block(AbstractBlock.Settings.copy(Blocks.STONE).mapColor(MapColor.BLACK)));

		if (!allSet.contains(blockA) || !allSet.contains(blockB)) {
			throw new AssertionError("Entries " + allSet + " found by RegistryMonitor via forAll were not as expected");
		}

		if (upcomingSet.contains(blockA) || !upcomingSet.contains(blockB)) {
			throw new AssertionError("Entries " + upcomingSet + " found by RegistryMonitor via forUpcoming were not as expected");
		}
	}
}
