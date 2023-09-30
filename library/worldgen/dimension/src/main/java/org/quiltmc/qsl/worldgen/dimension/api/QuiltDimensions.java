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

package org.quiltmc.qsl.worldgen.dimension.api;

import com.google.common.base.Preconditions;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;

import org.quiltmc.qsl.worldgen.dimension.impl.QuiltDimensionsImpl;

/**
 * This class contains methods that operate on world dimensions.
 */
public final class QuiltDimensions {
	// Static only-class, no instantiation necessary!
	private QuiltDimensions() {
		throw new UnsupportedOperationException("QuiltDimensions only contains static definitions.");
	}

	/**
	 * Directly teleports the specified entity to the specified location in another dimension,
	 * circumventing the built-in portal logic in vanilla.
	 * <p>
	 * Note: When teleporting a non-player entity to another dimension, it may be replaced with
	 * a new entity in the target dimension.
	 *
	 * @param entity           the entity to teleport
	 * @param destinationWorld the dimension to teleport the entity to
	 * @param location         the location to place the entity at after it is moved to the specified world.
	 *                         Just like in vanilla, the velocity is ignored. If this location is set to {@code null},
	 *                         the entity will not be teleported.
	 * @param <E>              the type of the entity that is being teleported
	 * @return the teleported entity in the destination dimension, which will either be a new entity or teleported,
	 * depending on the type of entity
	 * @apiNote this method may only be called on the main server thread
	 */
	public static <E extends Entity> @Nullable E teleport(Entity entity, ServerWorld destinationWorld, @Nullable TeleportTarget location) {
		Preconditions.checkNotNull(entity, "entity may not be null");
		Preconditions.checkNotNull(destinationWorld, "destinationWorld may not be null");
		Preconditions.checkArgument(!destinationWorld.isClient(), "This method may only be called from the server side");

		return QuiltDimensionsImpl.teleport(entity, destinationWorld, location);
	}
}
