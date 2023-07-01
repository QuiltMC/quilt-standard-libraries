/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.resource.loader.test;

import net.fabricmc.api.EnvType;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.MinecraftQuiltLoader;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleSynchronousResourceReloader;

public class ResourceReloaderTestMod implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceReloaderTestMod.class);

	private static boolean clientResources = false;
	private static boolean serverResources = false;

	@Override
	public void onInitialize(ModContainer mod) {
		this.setupClientReloadListeners(ResourceLoader.get(ResourceType.CLIENT_RESOURCES));
		this.setupServerReloadListeners(ResourceLoader.get(ResourceType.SERVER_DATA));

		// No lifecycle events yet
		ServerTickEvents.START.register(server -> {
			if (!clientResources && MinecraftQuiltLoader.getEnvironmentType() == EnvType.CLIENT) {
				throw new AssertionError("Client reload listener was not called.");
			}

			if (!serverResources) {
				throw new AssertionError("Server reload listener was not called.");
			}
		});
	}

	private void setupClientReloadListeners(ResourceLoader resourceLoader) {
		resourceLoader.addReloaderOrdering(ResourceLoaderTestMod.id("client_first"), ResourceLoaderTestMod.id("client_second"));

		resourceLoader.registerReloader(new SimpleSynchronousResourceReloader() {
			@Override
			public @NotNull Identifier getQuiltId() {
				return ResourceLoaderTestMod.id("client_second");
			}

			@Override
			public void reload(ResourceManager manager) {
				if (!clientResources) {
					throw new AssertionError("Second resource reloader was called before the first!");
				}

				LOGGER.info("Second client resource reloader is called.");
			}
		});

		resourceLoader.registerReloader(new SimpleSynchronousResourceReloader() {
			@Override
			public @NotNull Identifier getQuiltId() {
				return ResourceLoaderTestMod.id("client_first");
			}

			@Override
			public void reload(ResourceManager manager) {
				clientResources = true;

				LOGGER.info("First client resource reloader is called.");

				if (!ResourceLoaderTestMod.loadingClientResources) {
					LOGGER.error("Client resource start reload did not trigger!");
				} else {
					ResourceLoaderTestMod.loadingClientResources = false;
				}
			}
		});
	}

	private void setupServerReloadListeners(ResourceLoader resourceLoader) {
		resourceLoader.addReloaderOrdering(ResourceLoaderTestMod.id("server_first"), ResourceLoaderTestMod.id("server_second"));

		resourceLoader.registerReloader(new SimpleSynchronousResourceReloader() {
			@Override
			public @NotNull Identifier getQuiltId() {
				return ResourceLoaderTestMod.id("server_second");
			}

			@Override
			public void reload(ResourceManager manager) {
				if (!serverResources) {
					throw new AssertionError("Second resource reloader was called before the first!");
				}

				LOGGER.info("Second server resource reloader is called.");
			}
		});

		resourceLoader.registerReloader(new SimpleSynchronousResourceReloader() {
			@Override
			public @NotNull Identifier getQuiltId() {
				return ResourceLoaderTestMod.id("server_first");
			}

			@Override
			public void reload(ResourceManager manager) {
				serverResources = true;

				LOGGER.info("First server resource reloader is called.");

				if (!ResourceLoaderTestMod.loadingServerResources) {
					LOGGER.error("Server resource start reload did not trigger!");
				} else {
					ResourceLoaderTestMod.loadingServerResources = false;
				}
			}
		});
	}
}
