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

package org.quiltmc.qsl.resource.loader.test;

import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resource.MultiPackResourceManager;
import net.minecraft.resource.pack.ResourcePack;

import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;

public class ResourceLoaderEventsTestMod implements ResourceLoaderEvents.StartDataPackReload,
		ResourceLoaderEvents.EndDataPackReload {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLoaderEventsTestMod.class);

	@Override
	public void onStartDataPackReload(ResourceLoaderEvents.StartDataPackReload.Context context) {
		LOGGER.info("Preparing for data pack reload, current resource manager: {}, old resource manager: {}",
				context.resourceManager(), context.previousResourceManager()
		);

		ResourceLoaderTestMod.loadingServerResources = true;
	}

	@Override
	public void onEndDataPackReload(ResourceLoaderEvents.EndDataPackReload.Context context) {
		context.error().ifPresentOrElse(
				error -> LOGGER.error("Failed to reload on {} because {}.", Thread.currentThread(), error),
				() -> {
					LOGGER.info("Finished data pack reloading successfully on {} with registries {}.", Thread.currentThread(), context.dynamicRegistries());

					if (context.resourceManager() instanceof MultiPackResourceManager multiPackResourceManager) {
						LOGGER.info("Data packs: {}", multiPackResourceManager.streamResourcePacks()
								.map(ResourcePack::getName)
								.collect(Collectors.joining(", ")));
					}
				}
		);
	}
}
