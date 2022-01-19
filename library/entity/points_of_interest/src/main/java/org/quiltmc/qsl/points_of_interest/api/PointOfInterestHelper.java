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

import java.util.Set;
import java.util.function.Predicate;

import com.google.common.collect.ImmutableSet;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.poi.PointOfInterest;
import net.minecraft.world.poi.PointOfInterestType;
import org.quiltmc.qsl.points_of_interest.mixin.PointOfInterestTypeAccessor;

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
	 * @param blocks all blocks where a {@link PointOfInterest} of this type will be present, will apply to all of the {@link Block}'s {@link BlockState}
	 * @return a new {@link PointOfInterestType}
	 */
	public static PointOfInterestType register(Identifier id, int ticketCount, int searchDistance, Block... blocks) {
		final ImmutableSet.Builder<BlockState> builder = ImmutableSet.builder();

		for (Block block : blocks) {
			builder.addAll(block.getStateManager().getStates());
		}

		return register(id, create(id, ticketCount, searchDistance, builder.build()));
	}

	/**
	 * Creates and registers a {@link PointOfInterestType}.
	 *
	 * @param id The id of this {@link PointOfInterestType}
	 * @param ticketCount the max amount of tickets available to be checked out from searches
	 * @param completionCondition determines what {@link PointOfInterestType}s to find when searching using this one
	 *                            {@link PointOfInterestType#UNEMPLOYED} uses this to find villager workstations
	 * @param searchDistance the distance in blocks from the {@link PointOfInterest} a {@link net.minecraft.entity.mob.MobEntity} can be before considering it reached
	 * @param blocks all blocks where a {@link PointOfInterest} of this type will be present, will apply to all of the {@link Block}'s {@link BlockState}
	 * @return a new {@link PointOfInterestType}
	 */
	public static PointOfInterestType register(Identifier id, int ticketCount, Predicate<PointOfInterestType> completionCondition, int searchDistance, Block... blocks) {
		final ImmutableSet.Builder<BlockState> builder = ImmutableSet.builder();

		for (Block block : blocks) {
			builder.addAll(block.getStateManager().getStates());
		}

		return register(id, create(id, ticketCount, completionCondition, searchDistance, builder.build()));
	}

	/**
	 * Creates and registers a {@link PointOfInterestType}.
	 *
	 * @param id the id of this {@link PointOfInterestType}
	 * @param ticketCount the max amount of tickets available to be checked out from searches
	 * @param searchDistance the distance in blocks from the {@link PointOfInterest} a {@link net.minecraft.entity.mob.MobEntity} can be before considering it reached
	 * @param states all {@link BlockState block states} where a {@link PointOfInterest} of this type will be present
	 * @return a new {@link PointOfInterestType}
	 */
	public static PointOfInterestType register(Identifier id, int ticketCount, int searchDistance, Iterable<BlockState> states) {
		final ImmutableSet.Builder<BlockState> builder = ImmutableSet.builder();

		return register(id, create(id, ticketCount, searchDistance, builder.addAll(states).build()));
	}

	/**
	 * Creates and registers a {@link PointOfInterestType}.
	 *
	 * @param id the id of this {@link PointOfInterestType}
	 * @param ticketCount the max amount of tickets available to be checked out from searches
	 * @param completionCondition determines what {@link PointOfInterestType}s to find when searching using this one
	 *                            {@link PointOfInterestType#UNEMPLOYED} uses this to find villager workstations
	 * @param searchDistance the distance in blocks from the {@link PointOfInterest} a {@link net.minecraft.entity.mob.MobEntity} can be before considering it reached
	 * @param states all {@link BlockState block states} where a {@link PointOfInterest} of this type will be present
	 * @return a new {@link PointOfInterestType}
	 */
	public static PointOfInterestType register(Identifier id, int ticketCount, Predicate<PointOfInterestType> completionCondition, int searchDistance, Iterable<BlockState> states) {
		final ImmutableSet.Builder<BlockState> builder = ImmutableSet.builder();

		return register(id, create(id, ticketCount, completionCondition, searchDistance, builder.addAll(states).build()));
	}

	/**
	 * Creates and registers a {@link PointOfInterestType}.
	 *
	 * @param id the id of this {@link PointOfInterestType}
	 * @param ticketCount the max amount of tickets available to be checked out from searches
	 * @param searchDistance the distance in blocks from the {@link PointOfInterest} a {@link net.minecraft.entity.mob.MobEntity} can be before considering it reached
	 * @param states all {@link BlockState block states} where a {@link PointOfInterest} of this type will be present
	 * @return a new {@link PointOfInterestType}
	 */
	public static PointOfInterestType register(Identifier id, int ticketCount, int searchDistance, Set<BlockState> states) {
		return register(id, create(id, ticketCount, searchDistance, ImmutableSet.copyOf(states)));
	}

	/**
	 * Creates and registers a {@link PointOfInterestType}.
	 *
	 * @param id the id of this {@link PointOfInterestType}
	 * @param ticketCount the max amount of tickets available to be checked out from searches
	 * @param completionCondition determines what {@link PointOfInterestType}s to find when searching using this one
	 *                            {@link PointOfInterestType#UNEMPLOYED} uses this to find villager workstations
	 * @param searchDistance the distance in blocks from the {@link PointOfInterest} a {@link net.minecraft.entity.mob.MobEntity} can be before considering it reached
	 * @param states all {@link BlockState block states} where a {@link PointOfInterest} of this type will be present
	 * @return a new {@link PointOfInterestType}
	 */
	public static PointOfInterestType register(Identifier id, int ticketCount, Predicate<PointOfInterestType> completionCondition, int searchDistance, Set<BlockState> states) {
		return register(id, create(id, ticketCount, completionCondition, searchDistance, ImmutableSet.copyOf(states)));
	}

	/**
	 * Registers a {@link PointOfInterestType}.
	 *
	 * @param id the {@link Identifier} to register this {@link PointOfInterestType} under
	 * @param poiType the {@link PointOfInterestType} to register
	 * @return the given {@link PointOfInterestType}
	 */
	public static PointOfInterestType register(Identifier id, PointOfInterestType poiType) {
		return Registry.register(Registry.POINT_OF_INTEREST_TYPE, id, poiType);
	}

	/**
	 * Creates a {@link PointOfInterestType}.
	 *
	 * <p>You usually want to register a {@link PointOfInterestType} instead.</p>
	 *
	 * @param id the id of this {@link PointOfInterestType}
	 * @param ticketCount the max amount of tickets available to be checked out from searches
	 * @param searchDistance the distance in blocks from the {@link PointOfInterest} a {@link net.minecraft.entity.mob.MobEntity} can be before considering it reached
	 * @param states all {@link BlockState block states} where a {@link PointOfInterest} of this type will be present
	 * @return a new {@link PointOfInterestType}
	 * @see PointOfInterestHelper#register(Identifier, int, int, Set)
	 */
	public static PointOfInterestType create(Identifier id, int ticketCount, int searchDistance, Set<BlockState> states) {
		return PointOfInterestTypeAccessor.callSetup(
				PointOfInterestTypeAccessor.callCreate(id.toString(), states, ticketCount, searchDistance));
	}

	/**
	 * Creates a {@link PointOfInterestType}.
	 *
	 * <p>You usually want to register a {@link PointOfInterestType} instead.</p>
	 *
	 * @param id the id of this {@link PointOfInterestType}
	 * @param ticketCount the max amount of tickets available to be checked out from searches
	 * @param completionCondition determines what {@link PointOfInterestType}s to find when searching using this one
	 *                            {@link PointOfInterestType#UNEMPLOYED} uses this to find villager workstations
	 * @param searchDistance the distance in blocks from the {@link PointOfInterest} a {@link net.minecraft.entity.mob.MobEntity} can be before considering it reached
	 * @param states all {@link BlockState block states} where a {@link PointOfInterest} of this type will be present
	 * @return a new {@link PointOfInterestType}
	 * @see PointOfInterestHelper#register(Identifier, int, Predicate, int, Set)
	 */
	public static PointOfInterestType create(Identifier id, int ticketCount, Predicate<PointOfInterestType> completionCondition, int searchDistance, Set<BlockState> states) {
		return PointOfInterestTypeAccessor.callSetup(
				PointOfInterestTypeAccessor.callCreate(id.toString(), states, ticketCount, completionCondition, searchDistance));
	}
}
