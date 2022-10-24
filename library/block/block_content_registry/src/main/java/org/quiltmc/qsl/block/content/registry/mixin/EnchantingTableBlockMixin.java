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

package org.quiltmc.qsl.block.content.registry.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.EnchantingTableBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;
import org.quiltmc.qsl.block.content.registry.api.BlockContentRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Optional;

@Mixin(EnchantingTableBlock.class)
public class EnchantingTableBlockMixin {
	@Redirect(method = "isValidForBookshelf", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/BlockState;isOf(Lnet/minecraft/block/Block;)Z"))
	private static boolean hasEnchantmentPower(BlockState blockState, Block ignored) {
		return BlockContentRegistries.ENCHANTMENT_BOOSTERS.get(blockState.getBlock()).orElse(0f) != 0f;
	}

	@Redirect(method = "randomDisplayTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/random/RandomGenerator;nextInt(I)I"))
	private int changeParticleChance(RandomGenerator random, int bound) {
		return 0;
	}

	// Make particles spawn rate depend on the enchantment power level of the block
	@Redirect(method = "randomDisplayTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/EnchantingTableBlock;isValidForBookshelf(Lnet/minecraft/world/World;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/util/math/BlockPos;)Z"))
	private boolean changeParticleChance(World world, BlockPos pos, BlockPos offset, BlockState ignoredState, World ignoredWorld, BlockPos ignoredPos, RandomGenerator random) {
		if (!world.isAir(pos.add(offset.getX() / 2, offset.getY(), offset.getZ() / 2))) {
			return false;
		}

		Block block = world.getBlockState(pos.add(offset)).getBlock();
		Optional<Float> power = BlockContentRegistries.ENCHANTMENT_BOOSTERS.get(block);

		if (power.isPresent()) {
			return random.nextFloat() * 16f <= power.get();
		}
		return false;
	}

}
