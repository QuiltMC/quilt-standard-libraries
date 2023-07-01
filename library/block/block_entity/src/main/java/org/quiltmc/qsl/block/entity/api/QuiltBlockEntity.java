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

package org.quiltmc.qsl.block.entity.api;

import java.util.Objects;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;

/**
 * Extensions of {@link BlockEntity}, provides some utilities like easy syncing.
 */
public interface QuiltBlockEntity {
	/**
	 * Attempts to synchronize the block entity data to the client.
	 *
	 * @throws IllegalStateException if called on the logical client
	 * @throws NullPointerException  if there's no world associated with the block entity
	 */
	default void sync() {
		if (this instanceof BlockEntity blockEntity) {
			World world = blockEntity.getWorld();

			Objects.requireNonNull(world); // Maintain distinct failure case from below.
			if (world instanceof ServerWorld serverWorld) {
				serverWorld.getChunkManager().markForUpdate(blockEntity.getPos());
			} else {
				throw new UnsupportedOperationException("Cannot call sync() on the logical client!");
			}
		} else {
			throw new IllegalStateException("QuiltBlockEntity has been implemented onto a non-BlockEntity class, please override sync().");
		}
	}
}
