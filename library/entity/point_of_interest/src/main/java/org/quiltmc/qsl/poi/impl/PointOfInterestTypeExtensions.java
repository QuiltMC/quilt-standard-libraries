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

package org.quiltmc.qsl.poi.impl;

import java.util.Collection;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.RegistryKey;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestType;

@ApiStatus.Internal
public interface PointOfInterestTypeExtensions {
	/**
	 * Allows adding {@link Block}s after construction.
	 *
	 * @param key The {@link RegistryKey} associated with this {@link PointOfInterestType}
	 * @param blocks all additional blocks where a {@link PointOfInterest} of this type will be present.
	 *               Will apply to all of the {@link Block}'s {@link BlockState}s
	 */
	void quilt$addBlocks(RegistryKey<PointOfInterestType> key, Collection<Block> blocks);

	/**
	 * Allows adding {@link BlockState}s after construction.
	 *
	 * @param key The {@link RegistryKey} associated with this {@link PointOfInterestType}
	 * @param states all additional {@link BlockState block states} where a {@link PointOfInterest} of this type will be present
	 */
	void quilt$addBlockStates(RegistryKey<PointOfInterestType> key, Collection<BlockState> states);

	/**
	 * Replaces the existing {@link PointOfInterestType#blockStates} after construction.
	 *
	 * @param key The {@link RegistryKey} associated with this {@link PointOfInterestType}
	 * @param blocks all blocks where a {@link PointOfInterest} of this type will be present.
	 *               Will apply to all of the {@link Block}'s {@link BlockState}s
	 */
	void quilt$setBlocks(RegistryKey<PointOfInterestType> key, Collection<Block> blocks);

	/**
	 * Allows replacing {@link BlockState}s after construction.
	 *
	 * @param key The {@link RegistryKey} associated with this {@link PointOfInterestType}
	 * @param states all {@link BlockState block states} where a {@link PointOfInterest} of this type will be present
	 */
	void quilt$setBlockStates(RegistryKey<PointOfInterestType> key, Collection<BlockState> states);
}
