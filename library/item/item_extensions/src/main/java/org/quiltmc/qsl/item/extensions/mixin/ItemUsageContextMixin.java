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

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Property;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;

import org.quiltmc.qsl.item.extensions.api.QuiltItemUsageContextExtensions;

@Mixin(ItemUsageContext.class)
public abstract class ItemUsageContextMixin implements QuiltItemUsageContextExtensions {
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

	@Override
	public boolean isClientWorld() {
		return this.world.isClient;
	}

	@Override
	public @NotNull RandomGenerator getWorldRandom() {
		return this.world.getRandom();
	}

	@Override
	public @NotNull BlockState getBlockState() {
		return this.world.getBlockState(this.getBlockPos());
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
	public <T extends Comparable<T>> void setBlockProperty(@NotNull Property<T> property, @NotNull T newValue) {
		var pos = this.getBlockPos();
		var state = this.world.getBlockState(pos);
		state = state.with(property, newValue);
		this.replaceBlock(state);
	}

	@Override
	public void playSoundAtBlock(SoundEvent sound, SoundCategory category, float volume, float pitch) {
		this.world.playSound(this.player, this.getBlockPos(), sound, category, volume, pitch);
	}
}
