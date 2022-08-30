/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import org.quiltmc.qsl.entity.interaction.api.player.UseEntityEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net/minecraft/server/network/ServerPlayNetworkHandler$C_wsexhymd")
public abstract class ServerPlayNetworkHandlerMixin implements PlayerInteractEntityC2SPacket.Handler {

	@Shadow
	public ServerPlayNetworkHandler field_28963;

	@Shadow
	public Entity field_28962;

	@Inject(method = "interactAt(Lnet/minecraft/util/Hand;Lnet/minecraft/util/math/Vec3d;)V", at = @At("HEAD"), cancellable = true)
	private void beforePlayerInteractEntity(Hand hand, Vec3d hitPosition, CallbackInfo ci) {
		EntityHitResult hitResult = new EntityHitResult(field_28962, hitPosition.add(field_28962.getPos()));

		ActionResult result = UseEntityEvents.BEFORE.invoker().beforeUseEntity(field_28963.player, field_28963.player.world, hand, field_28963.player.getStackInHand(hand), field_28962, hitResult);

		if (result != ActionResult.PASS) ci.cancel();
	}

	@Inject(method = "interactAt(Lnet/minecraft/util/Hand;Lnet/minecraft/util/math/Vec3d;)V", at = @At("TAIL"))
	private void afterPlayerInteractEntity(Hand hand, Vec3d hitPosition, CallbackInfo ci) {
		EntityHitResult hitResult = new EntityHitResult(field_28962, hitPosition.add(field_28962.getPos()));

		UseEntityEvents.AFTER.invoker().afterUseEntity(field_28963.player, field_28963.player.world, hand, field_28963.player.getStackInHand(hand), field_28962, hitResult);
	}
}
