/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC, 2021 QuiltMC
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

package org.quiltmc.qsl.entity.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.quiltmc.qsl.entity.api.event.EntityWorldChangeEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
abstract class ServerPlayerEntityMixin extends PlayerEntity {
	public ServerPlayerEntityMixin(World world, BlockPos blockPos, float f, GameProfile gameProfile) {
		super(world, blockPos, f, gameProfile);
	}

	/**
	 * This is called by both "moveToWorld" and "teleport".
	 * So this is suitable to handle the after event from both call sites.
	 */
	@Inject(method = "worldChanged(Lnet/minecraft/server/world/ServerWorld;)V", at = @At("TAIL"))
	private void afterWorldChanged(ServerWorld origin, CallbackInfo ci) {
		EntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.invoker().afterChangeWorld((ServerPlayerEntity) (Object) this, origin, (ServerWorld) this.world);
	}
}
