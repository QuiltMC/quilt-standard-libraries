/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.entity.event.mixin;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MovementFlag;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import org.quiltmc.qsl.entity.event.api.EntityWorldChangeEvents;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Shadow public World world;

	@Inject(method = "moveToWorld", at = @At("RETURN"))
	private void quilt$afterWorldChanged(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
		// Ret will only have an entity if the teleport worked (entity not removed, teleportTarget was valid, entity was successfully created)
		Entity ret = cir.getReturnValue();

		if (ret != null) {
			EntityWorldChangeEvents.AFTER_ENTITY_WORLD_CHANGE.invoker().afterWorldChange((Entity) (Object) this, ret, (ServerWorld) this.world, (ServerWorld) ret.getWorld());
		}
	}

	@Inject(
			method = "teleport(Lnet/minecraft/server/world/ServerWorld;DDDLjava/util/Set;FF)Z",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setRemoved(Lnet/minecraft/entity/Entity$RemovalReason;)V"),
			locals = LocalCapture.CAPTURE_FAILEXCEPTION
	)
	private void quilt$afterWorldChangedByTeleport(ServerWorld destination, double x, double y, double z, Set<MovementFlag> relativeMovements, float yaw,
												   float pitch, CallbackInfoReturnable<Boolean> ci, float f, Entity newEntity) {
		EntityWorldChangeEvents.AFTER_ENTITY_WORLD_CHANGE.invoker().afterWorldChange((Entity) (Object) this, newEntity, ((ServerWorld) this.world), destination);
	}
}
