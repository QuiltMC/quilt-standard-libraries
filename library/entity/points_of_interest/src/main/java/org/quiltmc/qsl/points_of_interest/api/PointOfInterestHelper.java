/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.points_of_interest.api;

import static net.minecraft.world.poi.PointOfInterestTypes.STATE_TO_TYPE;

import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Holder;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestType;
import net.minecraft.world.poi.PointOfInterestTypes;

/**
 * This class provides utilities to create a {@link PointOfInterestType}.
 *
 * <p>A point of interest is typically used by villagers to specify their workstation blocks, meeting zones and homes.
 * Points of interest are also used by bees to specify where their bee hive is and nether portals to find existing portals.</p>
 */
public final class PointOfInterestHelper {

	/**
	 * Creates and registers a {@link PointOfInterestType}.
	 *
	 * @param id The id of this {@link PointOfInterestType}
	 * @param ticketCount the max amount of tickets available to be checked out from searches
	 * @param searchDistance the distance in blocks from the {@link PointOfInterest} a {@link net.minecraft.entity.mob.MobEntity} can be before considering it reached
	 * @param blocks all blocks where a {@link PointOfInterest} of this type will be present. Will apply to all of the {@link Block}'s {@link BlockState}s
	 * @return a new {@link RegistryKey} for the {@link PointOfInterestType}
	 */
	public static RegistryKey<PointOfInterestType> register(Identifier id, int ticketCount, int searchDistance, Block... blocks) {
		final ImmutableSet.Builder<BlockState> builder = ImmutableSet.builder();

		for (Block block : blocks) {
			builder.addAll(block.getStateManager().getStates());
		}

		return register(id, ticketCount, searchDistance, builder);
	}

	/**
	 * Creates and registers a {@link PointOfInterestType}.
	 *
	 * @param id the id of this {@link PointOfInterestType}
	 * @param ticketCount the max amount of tickets available to be checked out from searches
	 * @param searchDistance the distance in blocks from the {@link PointOfInterest} a {@link net.minecraft.entity.mob.MobEntity} can be before considering it reached
	 * @param states all {@link BlockState block states} where a {@link PointOfInterest} of this type will be present
	 * @return a new {@link RegistryKey} for the {@link PointOfInterestType}
	 */
	public static RegistryKey<PointOfInterestType> register(Identifier id, int ticketCount, int searchDistance, Iterable<BlockState> states) {
		final ImmutableSet.Builder<BlockState> builder = ImmutableSet.builder();

		return register(id, ticketCount, searchDistance, builder.addAll(states));
	}

	/**
	 * Creates and registers a {@link PointOfInterestType}.
	 *
	 * @param id the id of this {@link PointOfInterestType}
	 * @param ticketCount the max amount of tickets available to be checked out from searches
	 * @param searchDistance the distance in blocks from the {@link PointOfInterest} a {@link net.minecraft.entity.mob.MobEntity} can be before considering it reached
	 * @param states all {@link BlockState block states} where a {@link PointOfInterest} of this type will be present
	 * @return a new {@link RegistryKey} for the {@link PointOfInterestType}
	 */
	public static RegistryKey<PointOfInterestType> register(Identifier id, int ticketCount, int searchDistance, Set<BlockState> states) {
		return register(id, new PointOfInterestType(ImmutableSet.copyOf(states), ticketCount, searchDistance));
	}

	/**
	 * Creates and registers a {@link PointOfInterestType}.
	 *
	 * @param id the id of this {@link PointOfInterestType}
	 * @param ticketCount the max amount of tickets available to be checked out from searches
	 * @param searchDistance the distance in blocks from the {@link PointOfInterest} a {@link net.minecraft.entity.mob.MobEntity} can be before considering it reached
	 * @param states all {@link BlockState block states} where a {@link PointOfInterest} of this type will be present
	 * @return a new {@link RegistryKey} for the {@link PointOfInterestType}
	 */
	public static RegistryKey<PointOfInterestType> register(Identifier id, int ticketCount, int searchDistance, ImmutableSet.Builder<BlockState> states) {
		return register(id, new PointOfInterestType(states.build(), ticketCount, searchDistance));
	}

	/**
	 * Registers a {@link PointOfInterestType}.
	 *
	 * @param id the {@link Identifier} to register this {@link PointOfInterestType} under
	 * @param poiType the {@link PointOfInterestType} to register
	 * @return the {@link RegistryKey} for the {@link PointOfInterestType}
	 */
	public static RegistryKey<PointOfInterestType> register(Identifier id, PointOfInterestType poiType) {
		PointOfInterestTypes.ALL_STATES.addAll(poiType.blockStates());
		var key = RegistryKey.of(Registry.POINT_OF_INTEREST_TYPE_KEY, id);
		Registry.register(Registry.POINT_OF_INTEREST_TYPE, key, poiType);
		poiType.blockStates().forEach(state -> {
			Holder<PointOfInterestType> replaced = STATE_TO_TYPE.put(state, Registry.POINT_OF_INTEREST_TYPE.getHolderOrThrow(key));
			if (replaced != null) {
				throw Util.throwOrPause(new IllegalStateException(String.format("%s is defined in too many tags", state)));
			}
		});
		return key;
	}

	/**
	 * Allows adding {@link Block}s to a {@link PointOfInterestType} after construction.
	 *
	 * @param key the {@link RegistryKey<PointOfInterestType>} of the {@link PointOfInterestType} to add these blocks to
	 * @param blocks all additional {@link Block}s where a {@link PointOfInterest} of this type will be present. Will apply to all of the {@link Block}'s {@link BlockState}s
	 */
	public static void addBlocks(RegistryKey<PointOfInterestType> key, Block... blocks) {
		Registry.POINT_OF_INTEREST_TYPE.getOrEmpty(key).ifPresent(type -> type.addBlocks(key, Arrays.asList(blocks)));
	}

	/**
	 * Allows adding {@link Block}s to a {@link PointOfInterestType} after construction.
	 *
	 * @param key the {@link RegistryKey<PointOfInterestType>} of the {@link PointOfInterestType} to add these blocks to
	 * @param blocks all additional {@link Block}s where a {@link PointOfInterest} of this type will be present. Will apply to all of the {@link Block}'s {@link BlockState}s
	 */
	public static void addBlocks(RegistryKey<PointOfInterestType> key, Collection<Block> blocks) {
		Registry.POINT_OF_INTEREST_TYPE.getOrEmpty(key).ifPresent(type -> type.addBlocks(key, blocks));
	}

	/**
	 * Allows adding {@link Block}s to a {@link PointOfInterestType} after construction.
	 *
	 * @param key the {@link RegistryKey<PointOfInterestType>} of the {@link PointOfInterestType} to add these blocks to
	 * @param states all additional {@link BlockState block states} where a {@link PointOfInterest} of this type will be present
	 */
	public static void addBlockStates(RegistryKey<PointOfInterestType> key, BlockState... states) {
		Registry.POINT_OF_INTEREST_TYPE.getOrEmpty(key).ifPresent(type -> type.addBlockStates(key, Arrays.asList(states)));
	}

	/**
	 * Allows adding {@link Block}s to a {@link PointOfInterestType} after construction.
	 *
	 * @param key the {@link RegistryKey<PointOfInterestType>} of the {@link PointOfInterestType} to add these blocks to
	 * @param states all additional {@link BlockState block states} where a {@link PointOfInterest} of this type will be present
	 */
	public static void addBlockStates(RegistryKey<PointOfInterestType> key, Collection<BlockState> states) {
		Registry.POINT_OF_INTEREST_TYPE.getOrEmpty(key).ifPresent(type -> type.addBlockStates(key, states));
	}

	/**
	 * Allows replacing the {@link PointOfInterestType}s {@link Block}s after construction.
	 *
	 * @param key the {@link RegistryKey<PointOfInterestType>} of the {@link PointOfInterestType} to add these blocks to
	 * @param blocks all blocks where a {@link PointOfInterest} of this type will be present.
	 *               Will apply to all of the {@link Block}'s {@link BlockState}s
	 */
	public static void setBlocks(RegistryKey<PointOfInterestType> key, Block... blocks) {
		PointOfInterestHelper.setBlocks(key, Arrays.asList(blocks));
	}

	/**
	 * Allows replacing the {@link PointOfInterestType}s {@link Block}s after construction.
	 *
	 * @param key the {@link RegistryKey<PointOfInterestType>} of the {@link PointOfInterestType} to add these blocks to
	 * @param blocks all blocks where a {@link PointOfInterest} of this type will be present.
	 *               Will apply to all of the {@link Block}'s {@link BlockState}s
	 */
	public static void setBlocks(RegistryKey<PointOfInterestType> key, Collection<Block> blocks) {
		Registry.POINT_OF_INTEREST_TYPE.getOrEmpty(key).ifPresent(type -> type.setBlocks(key, blocks));
	}

	/**
	 * Allows replacing the {@link PointOfInterestType}s {@link BlockState}s after construction.
	 *
	 * @param key the {@link RegistryKey<PointOfInterestType>} of the {@link PointOfInterestType} to add these blocks to
	 * @param states all {@link BlockState block states} where a {@link PointOfInterest} of this type will be present
	 */
	public static void setBlockStates(RegistryKey<PointOfInterestType> key, BlockState... states) {
		PointOfInterestHelper.setBlockStates(key, Arrays.asList(states));
	}

	/**
	 * Allows replacing the {@link PointOfInterestType}s {@link BlockState}s after construction.
	 *
	 * @param key the {@link RegistryKey<PointOfInterestType>} of the {@link PointOfInterestType} to add these blocks to
	 * @param states all {@link BlockState block states} where a {@link PointOfInterest} of this type will be present
	 */
	public static void setBlockStates(RegistryKey<PointOfInterestType> key, Collection<BlockState> states) {
		Registry.POINT_OF_INTEREST_TYPE.getOrEmpty(key).ifPresent(type -> type.setBlockStates(key, states));
	}
}
