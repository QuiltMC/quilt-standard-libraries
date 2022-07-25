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

package org.quiltmc.qsl.rendering.item.api.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.item.Item;

/**
 * Provides methods for working with rendering items in inventories.
 */
@Environment(EnvType.CLIENT)
public final class QuiltItemRendering {
	private QuiltItemRendering() {
		throw new RuntimeException("QuiltItemRendering only contains static declarations.");
	}

	/**
	 * Checks if any of an item's overlay components are customized (non-vanilla).
	 *
	 * @param item the item
	 * @return {@code true} if item has custom overlay components, {@code false} otherwise
	 */
	@Environment(EnvType.CLIENT)
	public static boolean areOverlayComponentsCustomized(Item item) {
		return item.getCountLabelRenderer() != CountLabelRenderer.VANILLA
				|| item.getItemBarRenderers() != null
				|| item.getCooldownOverlayRenderer() != null;
	}
}
