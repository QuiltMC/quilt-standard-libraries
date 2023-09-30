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

package org.quiltmc.qsl.lifecycle.test.event.client;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

import net.minecraft.registry.RegistryKey;
import net.minecraft.world.World;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientWorldTickEvents;
import org.quiltmc.qsl.lifecycle.test.event.ServerLifecycleTests;

@ClientOnly
public final class ClientTickTests implements ClientModInitializer {
	private final Object2IntMap<RegistryKey<World>> tickTracker = new Object2IntOpenHashMap<>();
	private int ticks;

	@Override
	public void onInitializeClient(ModContainer mod) {
		ClientTickEvents.END.register(client -> {
			this.ticks++; // Just track our own tick since the client doesn't have a ticks value.

			if (this.ticks % 200 == 0) {
				ServerLifecycleTests.LOGGER.info("Ticked Client at " + this.ticks + " ticks.");
			}
		});

		ClientWorldTickEvents.END.register((client, world) -> {
			final int worldTicks = this.tickTracker.computeIfAbsent(world.getRegistryKey(), k -> 0);

			if (worldTicks % 200 == 0) { // Log every 200 ticks to verify the tick callback works on the client world
				ServerLifecycleTests.LOGGER.info("Ticked Client World - " + worldTicks + " ticks:" + world.getRegistryKey());
			}

			this.tickTracker.put(world.getRegistryKey(), worldTicks + 1);
		});
	}
}
