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

package org.quiltmc.qsl.block.content.registry.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.block.Block;
import net.minecraft.text.Text;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.block.content.registry.api.BlockContentRegistries;
import org.quiltmc.qsl.tooltip.api.client.ItemTooltipCallback;


public class BlockContentRegistriesClientInitializer implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("BlockContentRegistriesClientInitializer");

	public static final String ENABLE_TOOLTIP_DEBUG = "quilt.block.block_content_registry.enable_tooltip_debug";

	@Override
	public void onInitializeClient(ModContainer mod) {
		if (Boolean.getBoolean(ENABLE_TOOLTIP_DEBUG)) {
			if (QuiltLoader.isModLoaded("quilt_tooltip")) {
				ItemTooltipCallback.EVENT.register((stack, player, context, lines) -> {
					Block block = Block.getBlockFromItem(stack.getItem());

					BlockContentRegistries.FLATTENABLE_BLOCK.get(block).ifPresent(state -> lines.add(Text.literal("Flattenable block: " + state)));
					BlockContentRegistries.OXIDIZABLE_BLOCK.get(block).ifPresent(_block -> lines.add(Text.literal("Oxidizes to: " + _block.block())));
					BlockContentRegistries.WAXABLE_BLOCK.get(block).ifPresent(_block -> lines.add(Text.literal("Waxes to: " + _block.block())));
					BlockContentRegistries.STRIPPABLE_BLOCK.get(block).ifPresent(_block -> lines.add(Text.literal("Strips to: " + _block)));
					BlockContentRegistries.FLAMMABLE_BLOCK.get(block).ifPresent(entry -> lines.add(Text.literal("Flammable: " + entry.burn() + " burn chance, " + entry.spread() + " spread chance")));
				});
			} else {
				LOGGER.warn("Tooltip debug was enabled, but the QSL module `quilt_tooltip` was missing.");
			}
		}
	}
}
