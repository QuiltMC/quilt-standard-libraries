/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.entity.networking.test.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.CreeperEntityRenderer;
import net.minecraft.client.render.entity.EntityRendererFactory.Context;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.util.math.Axis;

import org.quiltmc.qsl.entity.networking.test.CreeperWithItem;

@Mixin(CreeperEntityRenderer.class)
public abstract class CreeperEntityRendererMixin extends MobEntityRenderer<CreeperEntity, CreeperEntityModel<CreeperEntity>> {
	public CreeperEntityRendererMixin(Context context, CreeperEntityModel<CreeperEntity> entityModel, float f) {
		super(context, entityModel, f);
	}

	@Override
	public void render(CreeperEntity creeper, float yaw, float tickDelta, MatrixStack matrixStack,
					   VertexConsumerProvider vertexConsumerProvider, int light) {
		super.render(creeper, yaw, tickDelta, matrixStack, vertexConsumerProvider, light);

		float rotation = (creeper.age + tickDelta) / 20;
		var stack = ((CreeperWithItem) creeper).getStack();
		var itemRenderer = MinecraftClient.getInstance().getItemRenderer();

		matrixStack.push();
		matrixStack.translate(0, 2, 0);
		matrixStack.scale(0.25f, 0.25f, 0.25f);
		matrixStack.multiply(Axis.Y_POSITIVE.rotation(rotation));
		itemRenderer.renderItem(
				stack, ModelTransformationMode.NONE, light, OverlayTexture.DEFAULT_UV,
				matrixStack, vertexConsumerProvider, creeper.getWorld(), 0
		);
		matrixStack.pop();
	}
}
