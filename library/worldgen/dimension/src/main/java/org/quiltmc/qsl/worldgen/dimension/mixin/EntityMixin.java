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

package org.quiltmc.qsl.worldgen.dimension.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;

import org.quiltmc.qsl.worldgen.dimension.impl.EntityAccess;

@Mixin(Entity.class)
public class EntityMixin implements EntityAccess {
	@Unique
	public TeleportTarget quilt$overriddenTeleportTarget = null;

	@Inject(method = "getTeleportTarget(Lnet/minecraft/server/world/ServerWorld;)Lnet/minecraft/world/TeleportTarget;", at = @At("HEAD"), cancellable = true)
	public void getOverriddenTeleportTarget(ServerWorld destination, CallbackInfoReturnable<TeleportTarget> cir) {
		if (this.quilt$overriddenTeleportTarget != null) {
			cir.setReturnValue(this.quilt$overriddenTeleportTarget);
		}
	}

	@Override
	public TeleportTarget getTeleportTarget() {
		return this.quilt$overriddenTeleportTarget;
	}

	@Override
	public void setTeleportTarget(TeleportTarget target) {
		this.quilt$overriddenTeleportTarget = target;
	}
}
