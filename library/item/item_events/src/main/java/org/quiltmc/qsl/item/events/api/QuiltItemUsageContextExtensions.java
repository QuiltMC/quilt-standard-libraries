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

package org.quiltmc.qsl.item.events.api;

import java.util.Optional;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvent;
import net.minecraft.state.property.Property;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.event.GameEvent;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

@InjectedInterface(ItemUsageContext.class)
public interface QuiltItemUsageContextExtensions {
	// impls are in ItemUsageContextMixin

	default @NotNull ActionResult success() {
		return ActionResult.SUCCESS;
	}

	default @NotNull RandomGenerator getWorldRandom() {
		return RandomGenerator.createLegacy();
	}

	default @NotNull BlockState getBlockState() {
		return Blocks.AIR.getDefaultState();
	}

	default @Nullable BlockEntity getBlockEntity() {
		return null;
	}

	@SuppressWarnings("unchecked")
	default <T extends BlockEntity> @NotNull Optional<T> getBlockEntity(@NotNull BlockEntityType<T> type) {
		var be = this.getBlockEntity();
		return be != null && be.getType() == type ? Optional.of((T) be) : Optional.empty();
	}

	default boolean canModifyWorld() {
		return true;
	}

	default boolean canDestroyBlock() {
		return true;
	}

	default boolean canPlaceOnBlock() {
		return true;
	}

	default void setStack(ItemStack stack) {}

	default void damageStack(int amount) {}

	default void damageStack() {
		damageStack(1);
	}

	/**
	 * Replaces the block state that the item was used on.
	 * <p>
	 * This method uses the {@link Block#REDRAW_ON_MAIN_THREAD} flag when setting the block state,
	 * and also fires the relevant {@linkplain net.minecraft.world.event.GameEvent#BLOCK_CHANGE game event}.
	 *
	 * @param newState the new block state
	 *
	 * @see net.minecraft.world.World#setBlockState(BlockPos, BlockState, int)
	 * @see net.minecraft.world.World#emitGameEvent(Entity, GameEvent, Vec3d)
	 */
	default void replaceBlockState(@NotNull BlockState newState) {}

	default <T extends Comparable<T>> void setBlockProperty(@NotNull Property<T> property, @NotNull T newValue) {
		replaceBlockState(getBlockState().with(property, newValue));
	}

	default void playSoundAtBlock(SoundEvent sound, SoundCategory category, float volume, float pitch, boolean useDistance) {}

	default void playSoundAtBlockFromPlayer(SoundEvent sound, SoundCategory category, float volume, float pitch) {}
}
