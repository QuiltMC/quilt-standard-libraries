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

package org.quiltmc.qsl.entity.impl;

import com.google.common.collect.ImmutableSet;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.feature_flags.FeatureFlagBitSet;

@ApiStatus.Internal
public class QuiltEntityType<T extends Entity> extends EntityType<T> {
	private final @Nullable Boolean alwaysUpdateVelocity;

	public QuiltEntityType(EntityType.EntityFactory<T> factory, SpawnGroup spawnGroup, boolean saveable, boolean summonable, boolean fireImmune, boolean spawnableFarFromPlayer, ImmutableSet<Block> spawnBlocks, EntityDimensions entityDimensions, int maxTrackDistance, int trackTickInterval, @Nullable Boolean alwaysUpdateVelocity, FeatureFlagBitSet requiredFlags) {
		super(factory, spawnGroup, saveable, summonable, fireImmune, spawnableFarFromPlayer, spawnBlocks, entityDimensions, maxTrackDistance, trackTickInterval, requiredFlags);
		this.alwaysUpdateVelocity = alwaysUpdateVelocity;
	}

	@Override
	public boolean alwaysUpdateVelocity() {
		if (this.alwaysUpdateVelocity != null) {
			return this.alwaysUpdateVelocity;
		}

		return super.alwaysUpdateVelocity();
	}
}
