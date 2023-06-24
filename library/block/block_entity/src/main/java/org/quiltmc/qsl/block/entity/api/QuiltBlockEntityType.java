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

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

/**
 * Extensions of {@link BlockEntityType}, allows to manipulate the supported block set.
 * <p>
 * Modifying the supported block set is only allowed before client finishes initializing, and before any logical server starts.
 * After this freeze adding any blocks will be disallowed.
 * <p>
 * Freezing happens at {@link org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents#STARTING}
 * and {@link org.quiltmc.qsl.lifecycle.api.client.event.ClientLifecycleEvents#READY} events in the
 * {@code quilt_block_entity:block_entity_freezing} phase, which is ordered after the default phase to avoid any race condition.
 */
@InjectedInterface(BlockEntityType.class)
public interface QuiltBlockEntityType {
	/**
	 * Adds a supported block to this block entity type.
	 *
	 * @param block the supported block
	 * @throws IllegalStateException if attempting to add the supported block too late,
	 *                               supported blocks are frozen at server start/after client init
	 */
	default void addSupportedBlock(Block block) {
		throw new UnsupportedOperationException("No implementation of addSupportedBlock could be found.");
	}

	/**
	 * Adds supported blocks to this block entity type.
	 *
	 * @param blocks the supported blocks
	 * @throws IllegalStateException if attempting to add the supported blocks too late,
	 *                               supported blocks are frozen at server start/after client init
	 */
	default void addSupportedBlocks(Block... blocks) {
		throw new UnsupportedOperationException("No implementation of addSupportedBlocks could be found.");
	}
}
