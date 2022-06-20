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

package org.quiltmc.qsl.item.content_registry.impl;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.item.content_registry.api.ItemContentRegistries;
import org.quiltmc.qsl.tooltip.api.client.ItemTooltipCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.item.Item;
import net.minecraft.text.Text;


public class ItemContentRegistriesClientInitializer implements ClientModInitializer {
	public static final Logger LOGGER = LoggerFactory.getLogger("ItemContentRegistriesClientInitializer");

	public static final String ENABLE_TOOLTIP_DEBUG = "quilt.item.content_registry.enable_tooltip_debug";

	@Override
	public void onInitializeClient(ModContainer mod) {
		if (Boolean.getBoolean(ENABLE_TOOLTIP_DEBUG) && QuiltLoader.isModLoaded("quilt_tooltip")) {
			ItemTooltipCallback.EVENT.register((stack, player, context, lines) -> {
				Item item = stack.getItem();

				ItemContentRegistries.FUEL_TIME.getValue(item).ifPresent(time -> lines.add(Text.literal("Fuel Time: " + time + " ticks")));
				ItemContentRegistries.COMPOST_CHANCE.getValue(item).ifPresent(chance -> lines.add(Text.literal("Compost chance: " + (chance * 100) + "%")));
			});
		} else if (Boolean.getBoolean(ENABLE_TOOLTIP_DEBUG)) {
			LOGGER.warn("Tooltip debug was enabled, but the QSL module `quilt_tooltip` was missing.");
		}
	}
}
