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
import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.rendering.registration.impl.client.ArmorRendererRegistryImpl;

/**
 * Armor renderers render worn armor items with custom code.
 * They may be used to render armor with special models or effects.
 *
 * <p>The renderers are registered with {@link ArmorRenderer#register(ItemConvertible, ArmorRenderer)}.
 */
@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface ArmorRenderer {
	/**
	 * Registers the armor renderer for the specified item.
	 *
	 * @param item		the item
	 * @param renderer	the renderer
	 * @throws IllegalArgumentException if the item already has a registered armor renderer
	 * @throws NullPointerException if either the item or the renderer is null
	 */
	static void register(ItemConvertible item, ArmorRenderer renderer) {
		ArmorRendererRegistryImpl.register(item, renderer);
	}

	/**
	 * Renders an armor part.
	 *
	 * @param matrices			the matrix stack
	 * @param vertexConsumers	the vertex consumer provider
	 * @param stack				the item stack of the armor item
	 * @param entity			the entity wearing the armor item
	 * @param slot				the equipment slot in which the armor stack is worn
	 * @param light				packed lightmap coordinates
	 * @param armorModel		the default armor model provided by the feature renderer
	 * @param featureRenderer	the feature renderer which invoked this renderer
	 */
	void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, ItemStack stack, LivingEntity entity, EquipmentSlot slot, int light, BipedEntityModel<?> armorModel, ArmorFeatureRenderer<?, ?, ?> featureRenderer);
}
