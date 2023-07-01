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

package org.quiltmc.qsl.entity.multipart.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;

import org.quiltmc.qsl.entity.multipart.api.EntityPart;
import org.quiltmc.qsl.entity.multipart.api.MultipartEntity;
import org.quiltmc.qsl.entity.multipart.impl.EntityPartTracker;

@Mixin(targets = "net/minecraft/server/world/ServerWorld$ServerEntityHandler")
public class ServerEntityHandlerMixin {
	@SuppressWarnings("InvalidInjectorMethodSignature")
	@ModifyConstant(
			method = {"startTracking(Lnet/minecraft/entity/Entity;)V", "stopTracking(Lnet/minecraft/entity/Entity;)V"},
			constant = @Constant(classValue = EnderDragonEntity.class, ordinal = 0)
	)
	private static boolean cancelEnderDragonCheck(Object targetObject, Class<?> classValue) {
		return false;
	}

	@Inject(
			method = "startTracking(Lnet/minecraft/entity/Entity;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;updateDynamicGameEventListener(Ljava/util/function/BiConsumer;)V")
	)
	private void startTrackingEntityParts(Entity entity, CallbackInfo ci) {
		if (entity instanceof MultipartEntity multipartEntity) {
			for (EntityPart<?> part : multipartEntity.getEntityParts()) {
				((EntityPartTracker) entity.getWorld()).quilt$getEntityParts().put(((Entity) part).getId(), (Entity) part);
			}
		}
	}

	@Inject(
			method = "stopTracking(Lnet/minecraft/entity/Entity;)V",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;updateDynamicGameEventListener(Ljava/util/function/BiConsumer;)V")
	)
	private void stopTrackingEntityParts(Entity entity, CallbackInfo ci) {
		if (entity instanceof MultipartEntity multipartEntity) {
			for (EntityPart<?> part : multipartEntity.getEntityParts()) {
				((EntityPartTracker) entity.getWorld()).quilt$getEntityParts().remove(((Entity) part).getId(), part);
			}
		}
	}
}
