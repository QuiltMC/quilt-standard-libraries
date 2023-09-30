/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.lifecycle.test.event;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;

/**
 * Test related to ticking events on the server.
 */
public final class ServerTickTests implements ModInitializer {
	private final Object2IntMap<RegistryKey<World>> tickTracker = new Object2IntOpenHashMap<>();

	@Override
	public void onInitialize(ModContainer mod) {
		ServerTickEvents.END.register(server -> {
			if (server.getTicks() % 200 == 0) { // Log every 200 ticks to verify the tick callback works on the server
				ServerLifecycleTests.LOGGER.info("Ticked Server at " + server.getTicks() + " ticks.");
			}
		});

		ServerWorldTickEvents.START.register((server, world) -> {
			final int worldTicks = this.tickTracker.computeIfAbsent(world.getRegistryKey(), k -> 0);

			if (worldTicks % 200 == 0) { // Log every 200 ticks to verify the tick callback works on the server world
				ServerLifecycleTests.LOGGER.info("Started ticking Server World - " + worldTicks);
			}
		});

		ServerWorldTickEvents.END.register((server, world) -> {
			final int worldTicks = this.tickTracker.computeIfAbsent(world.getRegistryKey(), k -> 0);

			if (worldTicks % 200 == 0) { // Log every 200 ticks to verify the tick callback works on the server world
				ServerLifecycleTests.LOGGER.info("Ticked Server World - " + worldTicks + " ticks:" + world.getRegistryKey().getValue());
			}

			this.tickTracker.put(world.getRegistryKey(), worldTicks + 1);
		});
	}
}
