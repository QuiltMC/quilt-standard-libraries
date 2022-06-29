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
import org.jetbrains.annotations.NotNull;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.component.impl.sync.ServerSyncHandler;
import org.quiltmc.qsl.component.impl.sync.header.SyncPacketHeader;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.networking.api.ServerLoginConnectionEvents;

@ApiStatus.Internal
public final class CommonInitializer implements ModInitializer {

	public static @NotNull Identifier id(@NotNull String id) {
		return new Identifier(ComponentsImpl.MODID, id);
	}

	@Override
	public void onInitialize(ModContainer mod) {
		SyncPacketHeader.registerDefaults();
		ServerSyncHandler.getInstance().registerPackets();
		ServerLoginConnectionEvents.QUERY_START.register(id("component_sync"), ServerSyncHandler.getInstance());

		ServerLifecycleEvents.STARTING.register(
				id("freeze_component_registries"),
				server -> ComponentsImpl.freezeRegistries()
		);
	}
}
