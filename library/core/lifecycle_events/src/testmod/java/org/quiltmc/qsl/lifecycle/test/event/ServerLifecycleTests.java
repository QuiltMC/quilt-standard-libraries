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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.server.MinecraftServer;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerLifecycleEvents;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldLoadEvents;

/**
 * Tests related to the lifecycle of a server.
 */
public final class ServerLifecycleTests implements ModInitializer,
		ServerLifecycleEvents.Starting, ServerLifecycleEvents.Ready,
		ServerLifecycleEvents.Stopped, ServerLifecycleEvents.Stopping {
	public static final ServerLifecycleTests TESTS = new ServerLifecycleTests();
	public static final Logger LOGGER = LoggerFactory.getLogger("LifecycleEventsTest");

	@Override
	public void onInitialize(ModContainer mod) {
		ServerWorldLoadEvents.LOAD.register((server, world) -> {
			LOGGER.info("Loaded world " + world.getRegistryKey().getValue().toString());
		});

		ServerWorldLoadEvents.UNLOAD.register((server, world) -> {
			LOGGER.info("Unloaded world " + world.getRegistryKey().getValue().toString());
		});
	}

	@Override
	public void startingServer(MinecraftServer server) {
		LOGGER.info("Starting Server!");
	}

	@Override
	public void readyServer(MinecraftServer server) {
		LOGGER.info("Started Server!");
	}

	@Override
	public void stoppingServer(MinecraftServer server) {
		LOGGER.info("Stopping Server!");
	}

	@Override
	public void exitServer(MinecraftServer server) {
		LOGGER.info("Stopped Server!");
	}
}
