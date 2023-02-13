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
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.quiltmc.qsl.entity.interaction.api.player.AttackBlockCallback;
import org.quiltmc.qsl.entity.interaction.api.player.BreakBlockEvents;
import org.quiltmc.qsl.entity.interaction.api.player.UseBlockEvents;
import org.quiltmc.qsl.entity.interaction.api.player.UseItemEvents;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
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

	@Inject(method = "interactItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;getCount()I", ordinal = 0), cancellable = true)
	private void beforePlayerInteractItem(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		ActionResult result = UseItemEvents.BEFORE.invoker().beforeUseItem(player, world, hand, player.getStackInHand(hand));

		if (result != ActionResult.PASS) cir.setReturnValue(result);
	}

	@Inject(method = "interactItem", at = @At(value = "RETURN", target = "Lnet/minecraft/util/TypedActionResult;getValue()Ljava/lang/Object;"), slice = @Slice(from = @At(value = "INVOKE", target = "Lnet/minecraft/util/TypedActionResult;getValue()Ljava/lang/Object;")))
	private void afterPlayerInteractItem(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		if (cir.getReturnValue() != ActionResult.FAIL) {
			UseItemEvents.AFTER.invoker().afterUseItem(player, world, hand, stack);
		}
	}

	@Inject(method = "interactBlock", at = @At("HEAD"), cancellable = true)
	private void beforePlayerInteractBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
		ActionResult result = UseBlockEvents.BEFORE.invoker().beforeUseBlock(player, world, hand, stack, hitResult.getBlockPos(), hitResult);

		if (result != ActionResult.PASS) cir.setReturnValue(result);
	}

	@Inject(method = "interactBlock", at = @At("RETURN"))
	private void afterPlayerInteractBlock(ServerPlayerEntity player, World world, ItemStack stack, Hand hand, BlockHitResult hitResult, CallbackInfoReturnable<ActionResult> cir) {
		if (cir.getReturnValue() != ActionResult.FAIL) {
			UseBlockEvents.AFTER.invoker().afterUseBlock(player, world, hand, stack, hitResult.getBlockPos(), hitResult);
		}
	}

	@Inject(method = "processBlockBreakingAction", at = @At("HEAD"), cancellable = true)
	private void onPlayerAttackBlock(BlockPos pos, PlayerActionC2SPacket.Action action, Direction direction, int worldHeight, int i, CallbackInfo ci) {
		if (action != PlayerActionC2SPacket.Action.START_DESTROY_BLOCK) return;
		ActionResult result = AttackBlockCallback.EVENT.invoker().onAttackBlock(this.player, this.world, this.player.getMainHandStack(), pos, direction);

		if (result != ActionResult.PASS) {
			this.player.networkHandler.sendPacket(new BlockUpdateS2CPacket(this.world, pos));

			BlockEntity blockEntity = this.world.getBlockEntity(pos);
			if (blockEntity != null) {
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
		boolean result = BreakBlockEvents.BEFORE.invoker().beforePlayerBreaksBlock(this.player, this.world, this.player.getMainHandStack(), pos, state, blockEntity);

		if (!result) {
			BreakBlockEvents.CANCELED.invoker().onCancelPlayerBreaksBlock(this.player, this.world, this.player.getMainHandStack(), pos, state, blockEntity);
			cir.setReturnValue(false);
		}
	}

	@Inject(method = "tryBreakBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/Block;onBroken(Lnet/minecraft/world/WorldAccess;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V"), locals = LocalCapture.CAPTURE_FAILHARD)
	private void afterPlayerBreakBlock(BlockPos pos, CallbackInfoReturnable<Boolean> cir, BlockState state, BlockEntity blockEntity) {
		BreakBlockEvents.AFTER.invoker().afterPlayerBreaksBlock(this.player, this.world, this.player.getMainHandStack(), pos, state, blockEntity);
	}
}
