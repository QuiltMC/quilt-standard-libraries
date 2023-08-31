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

package org.quiltmc.qsl.vehicle.api;

import net.minecraft.block.BlockState;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.util.math.BlockPos;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

/**
 * Provides custom comparator output for minecarts resting on detector rails.
 */
@InjectedInterface(AbstractMinecartEntity.class)
public interface MinecartComparatorLogic {
	/**
	 * Compute the comparator output of a detector rail when this minecart is resting
	 * on top of it. Called from {@link net.minecraft.block.DetectorRailBlock#getComparatorOutput}.
	 *
	 * @param state Block state of the rail
	 * @param pos Position of the rail
	 * @return A redstone power value {@literal >=} 0 to use, else a value {@literal <} 0 to try the next minecart with
	 * 	a registered logic. If no logic chooses to provide a value, vanilla's logic is invoked.
	 */
	default int getComparatorValue(BlockState state, BlockPos pos) {
		return -1;
	}
}
