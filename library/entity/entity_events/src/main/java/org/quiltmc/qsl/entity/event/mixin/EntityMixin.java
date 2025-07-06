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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.TeleportTarget;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

import org.quiltmc.qsl.entity.event.api.EntityWorldChangeEvents;

@Mixin(Entity.class)
abstract class EntityMixin {
	@Shadow
	private World world;

	@ModifyReturnValue(method = "teleportAcrossDimensions", at = @At("RETURN"))
	private @Nullable Entity quilt$invokeAfterWorldChange(Entity newEntity, ServerWorld world, TeleportTarget target) {
		if (newEntity != null) {
			EntityWorldChangeEvents.AFTER_ENTITY_WORLD_CHANGE.invoker()
					.afterWorldChange((Entity) (Object) this, newEntity, ((ServerWorld) this.world), target.newWorld());
		}

		return newEntity;
	}
}
