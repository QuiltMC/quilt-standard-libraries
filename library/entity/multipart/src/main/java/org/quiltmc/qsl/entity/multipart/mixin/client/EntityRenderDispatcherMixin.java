/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.entity.multipart.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.util.math.MathHelper;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.entity.multipart.api.EntityPart;
import org.quiltmc.qsl.entity.multipart.api.MultipartEntity;

@ClientOnly
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {
	@SuppressWarnings("InvalidInjectorMethodSignature")
	@ModifyConstant(method = "renderHitbox", constant = @Constant(classValue = EnderDragonEntity.class, ordinal = 0))
	private static boolean cancelEnderDragonCheck(Object targetObject, Class<?> classValue) {
		return false;
	}

	@Inject(
			method = "renderHitbox",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/render/WorldRenderer;drawBox(Lnet/minecraft/client/util/math/MatrixStack;Lcom/mojang/blaze3d/vertex/VertexConsumer;Lnet/minecraft/util/math/Box;FFFF)V",
					ordinal = 0,
					shift = At.Shift.AFTER
			)
	)
	private static void renderMultipartHitboxes(MatrixStack matrices, VertexConsumer vertices, Entity entity, float tickDelta, CallbackInfo ci) {
		if (entity instanceof MultipartEntity multipartEntity) {
			double entityX = -MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
			double entityY = -MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
			double entityZ = -MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());

			for (EntityPart<?> part : multipartEntity.getEntityParts()) {
				part.renderHitbox(matrices, vertices, entityX, entityY, entityZ, entity, tickDelta);
			}
		}
	}
}
