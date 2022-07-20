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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

@InjectedInterface(Item.class)
public interface QuiltItemRenderingExtensions {
	/**
	 * @return {@code true} to render the rest of overlay, {@code false} otherwise.
	 */
	@Environment(EnvType.CLIENT)
	default boolean preRenderOverlay(MatrixStack matrices, TextRenderer renderer, float zOffset, ItemStack stack) {
		return true;
	}

	@Environment(EnvType.CLIENT)
	default CountLabelRenderer getCountLabelRenderer() {
		return CountLabelRenderer.VANILLA;
	}

	@Environment(EnvType.CLIENT)
	default CooldownOverlayRenderer getCooldownOverlayRenderer() {
		return CooldownOverlayRenderer.VANILLA;
	}

	@Environment(EnvType.CLIENT)
	default ItemBarRenderer[] getItemBarRenderers() {
		return ItemBarRenderer.getDefaultRenderers();
	}

	@Environment(EnvType.CLIENT)
	default void postRenderOverlay(MatrixStack matrices, TextRenderer renderer, float zOffset, ItemStack stack) { }
}
