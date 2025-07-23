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

package org.quiltmc.qsl.entity.test.mixin.networking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.CreeperEntityRenderer;
import net.minecraft.client.render.entity.MobEntityRenderer;
import net.minecraft.client.render.entity.model.CreeperEntityModel;
import net.minecraft.client.render.entity.state.CreeperRenderState;
import net.minecraft.client.render.model.json.ModelTransformationMode;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Axis;

import org.quiltmc.qsl.entity.test.networking.CreeperStateWithItem;
import org.quiltmc.qsl.entity.test.networking.CreeperWithItem;

@Mixin(CreeperEntityRenderer.class)
abstract class CreeperEntityRendererMixin extends MobEntityRenderer<CreeperEntity, CreeperRenderState, CreeperEntityModel> {
	@SuppressWarnings("DataFlowIssue")
	private CreeperEntityRendererMixin() {
		super(null, null, 0);
		throw new AssertionError("dummy constructor called");
	}

	@Override
	public void render(CreeperRenderState state, MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light) {
		super.render(state, matrices, vertexConsumers, light);

		CreeperStateWithItem extendedState = (CreeperStateWithItem) state;
		float rotation = extendedState.quilt$getStackRotation();
		ItemStack stack = extendedState.quilt$getStack();

		matrices.push();
		matrices.translate(0, 2, 0);
		matrices.scale(0.25f, 0.25f, 0.25f);
		// method_22907 is rotate
		matrices.method_22907(Axis.Y_POSITIVE.rotation(rotation));
		MinecraftClient.getInstance().getItemRenderer().renderItem(
				stack, ModelTransformationMode.NONE, light, OverlayTexture.DEFAULT_UV,
				matrices, vertexConsumers, null, 0
		);
		matrices.pop();
	}

	@Inject(
			method = "updateState(Lnet/minecraft/entity/mob/CreeperEntity;"
				+ "Lnet/minecraft/client/render/entity/state/CreeperRenderState;F)V",
			at = @At("TAIL")
	)
	private void updateStack(
			CreeperEntity creeper, CreeperRenderState state, float tickDelta, CallbackInfo ci
	) {
		CreeperStateWithItem extendedState = (CreeperStateWithItem) state;
		extendedState.quilt$setStack(((CreeperWithItem) creeper).quilt$getStack());
		extendedState.quilt$setStackRotation((state.age + tickDelta) / 20);
	}
}
