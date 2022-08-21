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

package org.quiltmc.qsl.entity.interaction.mixin.client;

import net.minecraft.class_7204;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.math.Vec3d;
import org.quiltmc.qsl.entity.interaction.api.player.AttackEntityCallback;
import org.quiltmc.qsl.entity.interaction.api.player.UseBlockCallback;
import org.quiltmc.qsl.entity.interaction.api.player.UseEntityCallback;
import org.quiltmc.qsl.entity.interaction.api.player.UseItemCallback;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerInteractionManager.class)
public abstract class ClientPlayerInteractionManagerMixin {

	@Shadow @Final
	private ClientPlayNetworkHandler networkHandler;

	// Method sends a packet with a sequentially assigned id to the server. class_7204 builds the packet from what I can tell.
	@Shadow
	protected abstract void m_vvsqjptk(ClientWorld world, class_7204 arg);

	@Inject(method = "attackEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V", ordinal = 0), cancellable = true)
	private void onPlayerAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
		ActionResult result = AttackEntityCallback.EVENT.invoker().onAttack(player, player.world, Hand.MAIN_HAND, target);

		if (result != ActionResult.PASS) {
			if (result == ActionResult.SUCCESS) {
				this.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.attack(target, player.isSneaking()));
			}

			ci.cancel();
		}
	}

	@Inject(method = "interactItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"), cancellable = true)
	private void onPlayerInteractItem(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		TypedActionResult<ItemStack> result = UseItemCallback.EVENT.invoker().onUse(player, player.world, hand);

		if (result.getResult() != ActionResult.PASS) {
			if (result.getResult() == ActionResult.SUCCESS) {
				this.networkHandler.sendPacket(new PlayerMoveC2SPacket.Full(player.getX(), player.getY(), player.getZ(), player.getYaw(), player.getPitch(), player.isOnGround()));
				// method sends a packet with a sequentially assigned id to the server
				m_vvsqjptk((ClientWorld) player.world, i -> new PlayerInteractItemC2SPacket(hand, i));
			}

			cir.setReturnValue(result.getResult());
		}
	}

	@Inject(method = "interactBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerInteractionManager;m_vvsqjptk(Lnet/minecraft/client/world/ClientWorld;Lnet/minecraft/class_7204;)V"), cancellable = true)
	private void onPlayerInteractBlock(ClientPlayerEntity player, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
		ActionResult result = UseBlockCallback.EVENT.invoker().onUseBlock(player, player.world, hand, hitResult);

		if (result != ActionResult.PASS) {
			if (result == ActionResult.SUCCESS) {
				// method sends a packet with a sequentially assigned id to the server
				this.m_vvsqjptk((ClientWorld) player.world, i -> new PlayerInteractBlockC2SPacket(hand, hitResult, i));
			}

			cir.setReturnValue(result);
		}
	}

	@Inject(method = "interactEntityAtLocation", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/hit/EntityHitResult;getPos()Lnet/minecraft/util/math/Vec3d;"), cancellable = true)
	private void onPlayerInteractEntity(PlayerEntity player, Entity entity, EntityHitResult hitResult, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		ActionResult result = UseEntityCallback.EVENT.invoker().onUseEntity(player, player.world, hand, entity, hitResult);

		if (result != ActionResult.PASS) {
			if (result == ActionResult.SUCCESS) {
				Vec3d vec3d = hitResult.getPos().subtract(entity.getPos());
				this.networkHandler.sendPacket(PlayerInteractEntityC2SPacket.interactAt(entity, player.isSneaking(), hand, vec3d));
			}

			cir.setReturnValue(result);
		}
	}
}
