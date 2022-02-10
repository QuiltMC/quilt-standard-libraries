/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2021-2022 QuiltMC
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

import java.util.Collection;
import java.util.Collections;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleSynchronousResourceReloader;

public class ResourceReloaderTestMod implements ModInitializer {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceReloaderTestMod.class);

	private static boolean clientResources = false;
	private static boolean serverResources = false;

	@Override
	public void onInitialize() {
		this.setupClientReloadListeners();
		this.setupServerReloadListeners();

		// No lifecycle events yet
		ServerTickEvents.START.register(server -> {
			if (!clientResources && FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
				throw new AssertionError("Client reload listener was not called.");
			}

			if (!serverResources) {
				throw new AssertionError("Server reload listener was not called.");
			}
		});
	}

	private void setupClientReloadListeners() {
		ResourceLoader.get(ResourceType.CLIENT_RESOURCES).registerReloader(new SimpleSynchronousResourceReloader() {
			@Override
			public Identifier getQuiltId() {
				return ResourceLoaderTestMod.id("client_second");
			}

			@Override
			public void reload(ResourceManager manager) {
				if (!clientResources) {
					throw new AssertionError("Second resource reloader was called before the first!");
				}

				LOGGER.info("Second client resource reloader is called.");
			}

			@Override
			public Collection<Identifier> getQuiltDependencies() {
				return Collections.singletonList(ResourceLoaderTestMod.id("client_first"));
			}
		});

		ResourceLoader.get(ResourceType.CLIENT_RESOURCES).registerReloader(new SimpleSynchronousResourceReloader() {
			@Override
			public Identifier getQuiltId() {
				return ResourceLoaderTestMod.id("client_first");
			}

			@Override
			public void reload(ResourceManager manager) {
				clientResources = true;

				LOGGER.info("First client resource reloader is called.");
			}
		});
	}

	private void setupServerReloadListeners() {
		ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(new SimpleSynchronousResourceReloader() {
			@Override
			public Identifier getQuiltId() {
				return ResourceLoaderTestMod.id("server_second");
			}

			@Override
			public void reload(ResourceManager manager) {
				if (!serverResources) {
					throw new AssertionError("Second resource reloader was called before the first!");
				}

				LOGGER.info("Second server resource reloader is called.");
			}

			@Override
			public Collection<Identifier> getQuiltDependencies() {
				return Collections.singletonList(ResourceLoaderTestMod.id("server_first"));
			}
		});

		ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(new SimpleSynchronousResourceReloader() {
			@Override
			public Identifier getQuiltId() {
				return ResourceLoaderTestMod.id("server_first");
			}

			@Override
			public void reload(ResourceManager manager) {
				serverResources = true;

				LOGGER.info("First server resource reloader is called.");
			}
		});
	}
}
