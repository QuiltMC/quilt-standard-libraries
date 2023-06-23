/*
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

package org.quiltmc.qsl.block.content.registry.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;

import org.quiltmc.qsl.block.content.registry.api.BlockContentRegistries;

@Mixin(EnchantingTableBlock.class)
public class EnchantingTableBlockMixin {
	@Inject(method = "isValidForBookshelf", at = @At("HEAD"), cancellable = true)
	private static void quilt$hasEnchantmentPower(World world, BlockPos pos, BlockPos offset, CallbackInfoReturnable<Boolean> cir) {
		var blockPos = pos.add(offset);
		var state = world.getBlockState(blockPos);
		var power = BlockContentRegistries.ENCHANTING_BOOSTERS.get(state.getBlock())
				.map(booster -> booster.getEnchantingBoost(world, state, blockPos)).orElse(0f);
		var hasPower = power >= 0.0f && world.getBlockState(pos.add(offset.getX() / 2, offset.getY(), offset.getZ() / 2)).isIn(BlockTags.ENCHANTMENT_POWER_TRANSMITTER);

		if (hasPower) {
			cir.setReturnValue(true);
		}
	}

	@Redirect(method = "randomDisplayTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/random/RandomGenerator;nextInt(I)I"))
	private int quilt$changeParticleChance(RandomGenerator random, int bound) {
		return 0;
	}

	// Make particles spawn rate depend on the block's enchanting booster
	@Redirect(
			method = "randomDisplayTick",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/block/EnchantingTableBlock;isValidForBookshelf(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Z"
			)
	)
	private boolean quilt$changeParticleChance(
			World world, BlockPos pos, BlockPos offset, BlockState ignoredState, World ignoredWorld, BlockPos ignoredPos, RandomGenerator random
	) {
		if (!world.getBlockState(pos.add(offset.getX() / 2, offset.getY(), offset.getZ() / 2)).isIn(BlockTags.ENCHANTMENT_POWER_TRANSMITTER)) {
			return false;
		}

		var blockPos = pos.add(offset);
		var blockState = world.getBlockState(blockPos);
		var block = blockState.getBlock();
		var booster = BlockContentRegistries.ENCHANTING_BOOSTERS.getNullable(block);

		if (booster != null) {
			var power = booster.getEnchantingBoost(world, blockState, blockPos);
			return random.nextFloat() * 16f <= power;
		}

		return false;
	}
}
