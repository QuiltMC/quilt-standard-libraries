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

package org.quiltmc.qsl.entity.interaction.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.entity.interaction.api.player.AttackEntityEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends PlayerEntity {

	@Inject(method = "attack", at = @At("HEAD"), cancellable = true)
	private void beforePlayerAttackEntity(Entity target, CallbackInfo ci) {
		ActionResult result = AttackEntityEvents.BEFORE.invoker().beforeAttackEntity(this, this.world, this.getMainHandStack(), target);

		if (result != ActionResult.PASS) ci.cancel();
	}

	@Inject(method = "attack", at = @At("TAIL"))
	private void afterPlayerAttackEntity(Entity target, CallbackInfo ci) {
		AttackEntityEvents.AFTER.invoker().afterAttackEntity(this, this.world, this.getMainHandStack(), target);
	}

	// Ignore
	public ServerPlayerEntityMixin(World world, BlockPos blockPos, float f, GameProfile gameProfile, @Nullable PlayerPublicKey playerPublicKey) {
		super(world, blockPos, f, gameProfile, playerPublicKey);
	}
}
