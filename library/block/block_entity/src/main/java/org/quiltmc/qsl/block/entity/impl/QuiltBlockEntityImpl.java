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

package org.quiltmc.qsl.block.entity.impl;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.ListenerPhase;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;

@ApiStatus.Internal
@ListenerPhase(
		namespace = QuiltBlockEntityImpl.NAMESPACE, path = QuiltBlockEntityImpl.BLOCK_ENTITY_FREEZING_PHASE,
		callbackTarget = ServerLifecycleEvents.Starting.class
)
public final class QuiltBlockEntityImpl implements ModInitializer, ServerLifecycleEvents.Starting {
	public static final String NAMESPACE = "quilt_block_entity";
	public static final String BLOCK_ENTITY_FREEZING_PHASE = "block_entity_freezing";
	public static final Identifier BLOCK_ENTITY_FREEZING_PHASE_ID = new Identifier(NAMESPACE, BLOCK_ENTITY_FREEZING_PHASE);
	public static final QuiltBlockEntityImpl INSTANCE = new QuiltBlockEntityImpl();
	private boolean frozen = false;

	/**
	 * Sets whether adding supported blocks to {@link net.minecraft.block.entity.BlockEntityType} is allowed or not.
	 * <p>
	 * Mods that attempt try to add dynamic block registration at runtime might want to access this method,
	 * but please remember this is an implementation detail and may change at any time.
	 * <p>
	 * This freezing is done to avoid possible bad behavior from some mods.
	 * Freezing happens in a specific event phase to avoid any issue for those who might want to use lifecycle events.
	 *
	 * @param frozen {@code true} if modifying the supported blocks set is forbidden, or {@code false} otherwise
	 */
	public void setFrozen(boolean frozen) {
		this.frozen = frozen;
	}

	public void ensureCanModify() {
		if (this.frozen) {
			throw new IllegalStateException("BlockEntityType cannot be modified! Game states are already frozen.");
		}
	}

	@Override
	public void onInitialize(ModContainer mod) {
		ServerLifecycleEvents.STARTING.addPhaseOrdering(Event.DEFAULT_PHASE, BLOCK_ENTITY_FREEZING_PHASE_ID);
	}

	@Override
	public void startingServer(MinecraftServer server) {
		this.setFrozen(true);
	}
}
