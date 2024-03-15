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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;

public class RegistryLibEventsTest implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger("Quilt Registry Lib Events Test");

	private static final Identifier TEST_BLOCK_ID = new Identifier("quilt_registry_test_events", "event_test_block");

	private static boolean entryAddEventFoundBlock = false;

	@Override
	public void onInitialize(ModContainer mod) {
		RegistryEvents.getEntryAddEvent(Registries.BLOCK).register(context -> {
			LOGGER.info("Block {} id={} raw={} was registered in registry {}",
					context.value(), context.id(), context.rawId(), context.registry());

			if (TEST_BLOCK_ID.equals(context.id())) {
				entryAddEventFoundBlock = true;
			}
		});

		register(TEST_BLOCK_ID, new Block(AbstractBlock.Settings.method_9630(Blocks.STONE).mapColor(MapColor.BLACK)));

		if (!entryAddEventFoundBlock) {
			throw new AssertionError("Registry entry add event was not invoked on the registration of block with id " + TEST_BLOCK_ID);
		}
	}

	static <T extends Block> T register(Identifier id, T block) {
		return Registry.register(Registries.BLOCK, id, block);
	}
}
