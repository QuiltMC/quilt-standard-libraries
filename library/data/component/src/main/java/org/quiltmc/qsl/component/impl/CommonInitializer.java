/*
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

package org.quiltmc.qsl.component.impl;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.component.impl.event.CommonEventListener;
import org.quiltmc.qsl.component.impl.event.ComponentEventPhases;
import org.quiltmc.qsl.component.impl.sync.ServerSyncHandler;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;
import org.quiltmc.qsl.networking.api.ServerLoginConnectionEvents;

@ApiStatus.Internal
public final class CommonInitializer implements ModInitializer {
	public static final String MOD_ID = "quilt_component";

	public static Identifier id(String id) {
		return new Identifier(MOD_ID, id);
	}

	@Override
	public void onInitialize(ModContainer mod) {
		ServerSyncHandler.getInstance().registerPackets();

		ServerLoginConnectionEvents.QUERY_START.register(
				ComponentEventPhases.SYNC_COMPONENT_REGISTRY,
				CommonEventListener::onQueryStart
		);

		ServerLifecycleEvents.STARTING.register(
				ComponentEventPhases.FREEZE_COMPONENT_REGISTRIES,
				CommonEventListener::onServerStart
		);

		ServerTickEvents.END.register(
				ComponentEventPhases.TICK_LEVEL_CONTAINER,
				CommonEventListener::onServerTick
		);

		ServerWorldTickEvents.END.register(
				ComponentEventPhases.TICK_WORLD_CONTAINER,
				CommonEventListener::onServerWorldTick
		);
	}
}
