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
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

/**
 * Renders a count label.
 */
@FunctionalInterface
public interface CountLabelRenderer extends GuiRendererHelper {
	/**
	 * A {@code CountLabelRenderer} that replicates vanilla behavior.
	 */
	@Environment(EnvType.CLIENT)
	CountLabelRenderer VANILLA = new VanillaCountLabelRenderer();

	/**
	 * Renders the count label.
	 *
	 * @param matrices the matrices
	 * @param renderer the text renderer
	 * @param zOffset the Z offset
	 * @param stack the item stack
	 * @param override the label contents, or {@code null} to use the default contents
	 */
	@Environment(EnvType.CLIENT)
	void renderCountLabel(MatrixStack matrices, TextRenderer renderer, float zOffset, ItemStack stack,
						  @Nullable String override);
}
