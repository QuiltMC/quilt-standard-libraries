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

package org.quiltmc.qsl.item.rendering.api.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.item.rendering.impl.client.VanillaItemBarRenderer;

public interface ItemBarRenderer extends GuiRendererHelper {
	@Environment(EnvType.CLIENT)
	ItemBarRenderer VANILLA = VanillaItemBarRenderer.INSTANCE;

	@Environment(EnvType.CLIENT)
	static ItemBarRenderer[] getDefaultRenderers() {
		return new ItemBarRenderer[] { VANILLA };
	}

	@Environment(EnvType.CLIENT)
	boolean isItemBarVisible(ItemStack stack);

	@Environment(EnvType.CLIENT)
	void renderItemBar(MatrixStack matrices, TextRenderer renderer, float zOffset, ItemStack stack);
}
