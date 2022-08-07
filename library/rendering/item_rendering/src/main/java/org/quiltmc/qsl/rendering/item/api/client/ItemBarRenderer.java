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

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.rendering.item.impl.client.VanillaItemBarRenderer;

/**
 * Renders an item bar.
 */
public interface ItemBarRenderer {
	/**
	 * An {@code ItemBarRenderer} that replicates vanilla behavior.
	 */
	@Environment(EnvType.CLIENT)
	ItemBarRenderer VANILLA = VanillaItemBarRenderer.INSTANCE;

	/**
	 * Checks if the item bar should be rendered for the given {@code ItemStack}.
	 *
	 * @param stack the item stack
	 * @return {@code true} if the item bar should be rendered, {@code false} otherwise
	 */
	@Environment(EnvType.CLIENT)
	boolean isItemBarVisible(ItemStack stack);

	/**
	 * Renders the item bar for the given {@code ItemStack}.
	 *
	 * @param matrices         the matrices
	 * @param quadBatchManager the quad batch manager
	 * @param textRenderer     the text renderer
	 * @param zOffset          the Z offset
	 * @param stack            the item stack
	 */
	@Environment(EnvType.CLIENT)
	void renderItemBar(MatrixStack matrices, QuadBatchManager quadBatchManager, TextRenderer textRenderer,
					   float zOffset, ItemStack stack);
}
