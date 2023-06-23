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

package org.quiltmc.qsl.vehicle.test.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(TntMinecartEntity.class)
public abstract class TntMinecartEntityMixin extends AbstractMinecartEntity {
	protected TntMinecartEntityMixin(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public int getComparatorValue(BlockState state, BlockPos pos) {
		return 15;
	}
}
