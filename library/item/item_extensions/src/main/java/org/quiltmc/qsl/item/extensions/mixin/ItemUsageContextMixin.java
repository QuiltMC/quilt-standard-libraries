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

package org.quiltmc.qsl.item.extensions.mixin;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.pattern.CachedBlockPosition;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import org.quiltmc.qsl.item.extensions.api.QuiltItemUsageContextExtensions;

@Mixin(ItemUsageContext.class)
public abstract class ItemUsageContextMixin implements QuiltItemUsageContextExtensions {
	@Unique
	private CachedBlockPosition quilt$cachedPos;

	@Shadow
	@Final
	private @Nullable PlayerEntity player;

	@Shadow
	@Final
	private Hand hand;

	@Shadow
	@Final
	private ItemStack stack;

	@Shadow
	@Final
	private World world;

	@Shadow
	public abstract BlockPos getBlockPos();

	@Inject(method = "<init>(Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;Lnet/minecraft/item/ItemStack;Lnet/minecraft/util/hit/BlockHitResult;)V",
			at = @At("RETURN"))
	private void quilt$init(World world, PlayerEntity playerEntity, Hand hand, ItemStack itemStack,
			BlockHitResult blockHitResult, CallbackInfo ci) {
		this.quilt$cachedPos = new CachedBlockPosition(world, blockHitResult.getBlockPos(), false);
	}

	@Override
	public @NotNull ActionResult success() {
		return ActionResult.success(this.world.isClient);
	}

	@Override
	public @NotNull RandomGenerator getWorldRandom() {
		return this.world.getRandom();
	}

	@Override
	public @NotNull BlockState getBlockState() {
		return this.quilt$cachedPos.getBlockState();
	}

	@Override
	public boolean canModifyWorld() {
		if (player == null) {
			return this.world.getGameRules().getBoolean(GameRules.DO_MOB_GRIEFING);
		} else {
			return this.player.getAbilities().allowModifyWorld;
		}
	}

	@Override
	public boolean canDestroyBlock() {
		return this.canModifyWorld() && this.stack.canDestroy(this.world.getRegistryManager().get(Registry.BLOCK_KEY),
				this.quilt$cachedPos);
	}

	@Override
	public boolean canPlaceOnBlock() {
		return this.canModifyWorld() && this.stack.canPlaceOn(this.world.getRegistryManager().get(Registry.BLOCK_KEY),
				this.quilt$cachedPos);
	}

	@Override
	public void damageStack(int amount) {
		if (this.player != null) {
			this.stack.damage(amount, player, playerx -> playerx.sendToolBreakStatus(hand));
		}
	}

	@Override
	public void replaceBlock(@NotNull BlockState newState) {
		var pos = this.getBlockPos();
		this.world.setBlockState(pos, newState, Block.NOTIFY_ALL | Block.REDRAW_ON_MAIN_THREAD);
		this.world.emitGameEvent(player, GameEvent.BLOCK_CHANGE, pos);
	}

	@Override
	public void playSoundAtBlock(SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance) {
		var pos = getBlockPos();
		this.world.playSound((double)pos.getX() + 0.5, (double)pos.getY() + 0.5, (double)pos.getZ() + 0.5,
				sound, category, volume, pitch, useDistance);
	}

	@Override
	public void playSoundAtBlockFromPlayer(SoundEvent sound, SoundCategory category, float volume, float pitch) {
		this.world.playSound(this.player, this.getBlockPos(), sound, category, volume, pitch);
	}
}
