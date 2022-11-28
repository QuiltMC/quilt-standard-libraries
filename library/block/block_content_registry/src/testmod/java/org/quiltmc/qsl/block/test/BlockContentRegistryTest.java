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

package org.quiltmc.qsl.block.test;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.Oxidizable;
import net.minecraft.block.OxidizableBlock;
import net.minecraft.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.BuiltinRegistries;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.content.registry.api.BlockContentRegistries;
import org.quiltmc.qsl.block.content.registry.api.FlammableBlockEntry;
import org.quiltmc.qsl.block.content.registry.api.ReversibleBlockEntry;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;
import org.quiltmc.qsl.registry.attachment.api.RegistryExtensions;

public class BlockContentRegistryTest implements ModInitializer {
	public static final String MOD_ID = "quilt_block_content_registry_testmod";
	public static final Logger LOGGER = LoggerFactory.getLogger("BlockContentRegistryTest");

	public static boolean testPassed = false;

	@Override
	public void onInitialize(ModContainer mod) {
		RegistryExtensions.register(BuiltinRegistries.BLOCK, new Identifier(MOD_ID, "oxidizable_iron_block"),
				new OxidizableBlock(Oxidizable.OxidizationLevel.UNAFFECTED, AbstractBlock.Settings.copy(Blocks.IRON_BLOCK)),
				BlockContentRegistries.OXIDIZABLE, new ReversibleBlockEntry(Blocks.IRON_BLOCK, false));

		BlockContentRegistries.ENCHANTING_BOOSTERS.put(Blocks.IRON_BLOCK, 3f);
		BlockContentRegistries.ENCHANTING_BOOSTERS.put(Blocks.DIAMOND_BLOCK, 15f);
		BlockContentRegistries.ENCHANTING_BOOSTERS.put(Blocks.NETHERITE_BLOCK, 100f);
		BlockContentRegistries.ENCHANTING_BOOSTERS.put(Blocks.OAK_PLANKS, 0.25f);

		ServerWorldTickEvents.START.register((server, world) -> {
			if (testPassed) {
				return;
			}

			LOGGER.info("Starting BlockContentRegistry tests");
			BuiltinRegistries.BLOCK.getOrCreateTag(BlockTags.ANVIL).forEach(holder -> this.assertValues(holder.value(), BlockContentRegistries.FLAMMABLE, new FlammableBlockEntry(100, 100)));

			this.assertValues(Blocks.OAK_PLANKS, BlockContentRegistries.FLATTENABLE, Blocks.OAK_SLAB.getDefaultState());
			this.assertValues(Blocks.QUARTZ_PILLAR, BlockContentRegistries.STRIPPABLE, Blocks.PURPUR_PILLAR);
			this.assertValues(Blocks.IRON_BLOCK, BlockContentRegistries.WAXABLE, new ReversibleBlockEntry(Blocks.GOLD_BLOCK, true));
			LOGGER.info("Finished BlockContentRegistry tests");

			testPassed = true;
		});
	}

	private <T> void assertValues(Block block, RegistryEntryAttachment<Block, T> attachment, T value) {
		Optional<T> entry = attachment.get(block);
		Identifier id = BuiltinRegistries.BLOCK.getId(block);
		if (entry.isEmpty()) {
			throw new AssertionError("No entry present for " + id);
		}

		if (!entry.get().equals(value)) {
			throw new AssertionError("Value incorrect for " + id);
		}

		LOGGER.info("Test for block " + id + " passed for REA " + attachment.id());
	}
}
