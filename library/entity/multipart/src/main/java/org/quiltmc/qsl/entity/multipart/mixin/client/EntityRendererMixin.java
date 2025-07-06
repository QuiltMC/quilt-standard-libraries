/*
 * Copyright 2025 The Quilt Project
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

import com.google.common.collect.ImmutableList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReceiver;

import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.Hitbox;
import net.minecraft.util.math.MathHelper;
import net.minecraft.entity.Entity;

import org.quiltmc.qsl.entity.multipart.api.EntityPart;
import org.quiltmc.qsl.entity.multipart.api.MultipartEntity;

@Mixin(EntityRenderer.class)
abstract class EntityRendererMixin<T extends Entity> {
	@ModifyReceiver(
			// method_68835 is createHitboxView
			method = "method_68835",
			at = @At(
				value = "INVOKE", remap = false,
				target = "Lcom/google/common/collect/ImmutableList$Builder;build()Lcom/google/common/collect/ImmutableList;"
			)
	)
	private ImmutableList.Builder<Hitbox> addMultipartHitboxes(
			ImmutableList.Builder<Hitbox> hitboxes,
			T entity, float tickDelta, boolean green
	) {
		if (entity instanceof MultipartEntity multipartEntity) {
			final double entityX = -MathHelper.lerp(tickDelta, entity.lastRenderX, entity.getX());
			final double entityY = -MathHelper.lerp(tickDelta, entity.lastRenderY, entity.getY());
			final double entityZ = -MathHelper.lerp(tickDelta, entity.lastRenderZ, entity.getZ());

			for (final EntityPart<?> part : multipartEntity.qsl$getEntityParts()) {
				part.getHitbox(entityX, entityY, entityZ, entity, tickDelta).ifPresent(hitboxes::add);
			}
		}

		return hitboxes;
	}
}
