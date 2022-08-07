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
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

/**
 * Provides extension methods to customize the rendering of an item in a GUI.
 */
@InjectedInterface(Item.class)
public interface QuiltItemRenderingExtensions {
	/**
	 * Called before the item's overlay is rendered.
	 *
	 * @param matrices         the matrices
	 * @param quadBatchManager the quad batch manager
	 * @param textRenderer     the text renderer
	 * @param zOffset          the Z offset
	 * @param stack            the item stack
	 * @return {@code true} to render the rest of the overlay, {@code false} otherwise.
	 */
	@Environment(EnvType.CLIENT)
	default boolean preRenderOverlay(MatrixStack matrices, QuadBatchManager quadBatchManager, TextRenderer textRenderer,
									 float zOffset, ItemStack stack) {
		return true;
	}

	/**
	 * {@return the count label renderer associated with this item}
	 */
	@Environment(EnvType.CLIENT)
	default CountLabelRenderer getCountLabelRenderer() {
		return CountLabelRenderer.VANILLA;
	}

	/**
	 * {@return the cooldown overlay renderer associated with this item}
	 */
	@Environment(EnvType.CLIENT)
	default CooldownOverlayRenderer getCooldownOverlayRenderer() {
		return CooldownOverlayRenderer.VANILLA;
	}

	/**
	 * Gets the item bar renderers associated with this item.
	 *
	 * @return array of item bar renderers, or {@code null} to use the vanilla renderer only
	 */
	@Environment(EnvType.CLIENT)
	default ItemBarRenderer @Nullable [] getItemBarRenderers() {
		return null;
	}

	/**
	 * Called after an item's overlay is rendered.
	 *
	 * @param matrices         the matrices
	 * @param quadBatchManager the quad batch manager
	 * @param textRenderer     the text renderer
	 * @param zOffset          the Z offset
	 * @param stack            the item stack
	 */
	@Environment(EnvType.CLIENT)
	default void postRenderOverlay(MatrixStack matrices, QuadBatchManager quadBatchManager, TextRenderer textRenderer,
								   float zOffset, ItemStack stack) { }
}
