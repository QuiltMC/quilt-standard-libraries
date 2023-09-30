/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.vehicle.mixin;

import java.util.List;
import java.util.function.Predicate;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.BlockState;
import net.minecraft.block.DetectorRailBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.quiltmc.qsl.vehicle.api.MinecartComparatorLogic;

@Mixin(DetectorRailBlock.class)
public abstract class DetectorRailBlockMixin {
	@Shadow
	protected abstract <T extends AbstractMinecartEntity> List<T> getCarts(World world, BlockPos pos, Class<T> entityClass, @Nullable Predicate<Entity> entityPredicate);

	@Inject(at = @At("HEAD"), method = "getComparatorOutput", cancellable = true)
	private void getCustomComparatorOutput(BlockState state, World world, BlockPos pos, CallbackInfoReturnable<Integer> cir) {
		if (state.get(DetectorRailBlock.POWERED)) {
			List<AbstractMinecartEntity> carts = this.getCarts(world, pos, AbstractMinecartEntity.class,
					cart -> ((MinecartComparatorLogic) cart).getComparatorValue(state, pos) >= 0);

			if (carts.isEmpty()) return;

			cir.setReturnValue(carts.get(0).getComparatorValue(state, pos));
		}
	}
}
