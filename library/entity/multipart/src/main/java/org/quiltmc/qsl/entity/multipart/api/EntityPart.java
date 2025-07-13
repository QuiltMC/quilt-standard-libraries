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

package org.quiltmc.qsl.entity.multipart.api;

import java.util.Optional;

import net.minecraft.client.render.entity.Hitbox;
import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.util.math.Box;

import org.quiltmc.loader.api.minecraft.ClientOnly;

/**
 * Represents the sub-parts of a {@link MultipartEntity}.
 *
 * @param <E> the {@link Entity} that owns this {@link EntityPart}
 * @see EnderDragonPart
 */
public interface EntityPart<E extends Entity> {
	E getOwner();

	/**
	 * Gets the hitbox for the entity part.
	 *
	 * <p>Should normally not be overridden unless it is to more accurately draw non-standard hitboxes.
	 *
	 * @param ownerX    the {@linkplain #getOwner() owner's} rendered X coordinate
	 * @param ownerY    the {@linkplain #getOwner() owner's} rendered Y coordinate
	 * @param ownerZ    the {@linkplain #getOwner() owner's} rendered Z coordinate
	 * @param owner     the {@linkplain #getOwner() owner}
	 * @param tickDelta progress for linearly interpolating between the previous and current game state
	 */
	@ClientOnly
	default Optional<Hitbox> getHitbox(double ownerX, double ownerY, double ownerZ, Entity owner, float tickDelta) {
		if (this instanceof Entity entityPart) {
			Box bounds = entityPart.getBounds().offset(
					-entityPart.getX(),
					-entityPart.getY(),
					-entityPart.getZ()
			);

			return Optional.of(new Hitbox(
				bounds.minX, bounds.minY, bounds.minZ,
				bounds.maxX, bounds.maxY, bounds.maxZ,
				0.25F, 1.0F, 0.0F
			));
		} else {
			return Optional.empty();
		}
	}
}
