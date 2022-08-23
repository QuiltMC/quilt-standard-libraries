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

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.network.ServerPlayerInteractionManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.quiltmc.qsl.entity.interaction.api.player.AttackBlockCallback;
import org.quiltmc.qsl.entity.interaction.api.player.PlayerBreakBlockEvents;
import org.quiltmc.qsl.entity.interaction.api.player.UseBlockCallback;
import org.quiltmc.qsl.entity.interaction.api.player.UseItemCallback;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(ServerPlayerInteractionManager.class)
public class ServerPlayerInteractionManagerMixin {

	@Shadow
	@Final
	protected ServerPlayerEntity player;

	@Shadow
	protected ServerWorld world;

	@Inject(method = "interactItem", at = @At("HEAD"), cancellable = true)
	private void onPlayerInteractItem(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		if (player.isSpectator()) return;

		TypedActionResult<ItemStack> result = UseItemCallback.EVENT.invoker().onUseItem(player, world, hand);

		if (result.getResult() != ActionResult.PASS) cir.setReturnValue(result.getResult());
	}

	@Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
	private void onPlayerInteractBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
		ActionResult result = UseBlockCallback.EVENT.invoker().onUseBlock(player, world, hand, hitResult);

		if (result != ActionResult.PASS) cir.setReturnValue(result);
	}

	@Inject(method = "processBlockBreakingAction", at = @At("HEAD"), cancellable = true)
	private void onPlayerAttackBlock(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int i, CallbackInfo ci) {
		if (action != PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) return;
		ActionResult result = AttackBlockCallback.EVENT.invoker().onAttackBlock(this.player, this.world, Hand.MAIN_HAND, pos, direction);

		if (result != ActionResult.PASS) {
			this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.world, pos));

			BlockEntity blockEntity;
			if ((blockEntity = this.world.getBlockEntity(pos)) != null) {
				Packet<ClientPlayPacketListener> packet = blockEntity.toUpdatePacket();

				if (packet != null) {
					this.player.networkHandler.sendPacket(packet);
				}
			}

			ci.cancel();
		}
	}

	@Inject(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBreak(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;Lnet/minecraft/entity/player/PlayerEntity;)V"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void onPlayerBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir, BlockState state, BlockEntity blockEntity) {
		boolean result = PlayerBreakBlockEvents.BEFORE.invoker().beforePlayerBreakBlock(this.player, this.world, pos, state, blockEntity);

		if (!result) {
			PlayerBreakBlockEvents.CANCELED.invoker().cancelPlayerBreakBlock(this.player, this.world, pos, state, blockEntity);
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBroken(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void afterPlayerBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir, BlockState state, BlockEntity blockEntity) {
		PlayerBreakBlockEvents.AFTER.invoker().afterPlayerBreakBlock(this.player, this.world, pos, state, blockEntity);
	}
}
