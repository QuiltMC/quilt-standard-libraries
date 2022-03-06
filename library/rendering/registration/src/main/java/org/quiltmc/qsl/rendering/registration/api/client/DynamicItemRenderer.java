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

package org.quiltmc.qsl.rendering.registration.api.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.model.json.ModelTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.rendering.registration.impl.client.DynamicItemRendererRegistryImpl;

/**
 * Dynamic item renderers render items with custom code.
 * They allow using non-model rendering, such as BERs, for items.
 *
 * <p>An item with a dynamic renderer must have a model extending {@code minecraft:builtin/entity}.
 * The renderers are registered with {@link DynamicItemRenderer#register(ItemConvertible, DynamicItemRenderer)}.
 */
@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface DynamicItemRenderer {
	/**
	 * Registers the renderer for the item.
	 *
	 * <p>Note that the item's JSON model must also extend {@code minecraft:builtin/entity}.
	 *
	 * @param item     the item
	 * @param renderer the renderer
	 * @throws IllegalArgumentException if the item already has a registered renderer
	 * @throws NullPointerException if either the item or the renderer is null
	 */
	static void register(ItemConvertible item, DynamicItemRenderer renderer) {
		DynamicItemRendererRegistryImpl.register(item, renderer);
	}

	/**
	 * Renders an item stack.
	 *
	 * @param stack           the rendered item stack
	 * @param mode            the model transformation mode
	 * @param matrices        the matrix stack
	 * @param vertexConsumers the vertex consumer provider
	 * @param light           packed lightmap coordinates
	 * @param overlay         the overlay UV passed to {@link net.minecraft.client.render.VertexConsumer#overlay(int)}
	 */
	void render(ItemStack stack, ModelTransformation.Mode mode, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, int overlay);
}
