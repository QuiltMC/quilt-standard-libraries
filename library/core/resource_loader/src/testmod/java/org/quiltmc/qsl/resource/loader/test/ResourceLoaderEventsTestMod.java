/*
 * Copyright 2021 QuiltMC
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

import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;

public class ResourceLoaderEventsTestMod implements ModInitializer {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onInitialize() {
		ResourceLoaderEvents.START_DATA_PACK_RELOAD.register((server, oldResourceManager) ->
				LOGGER.info("Preparing for data pack reload, old resource manager: {}", oldResourceManager));
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.register((server, resourceManager, error) -> {
			if (error == null) {
				LOGGER.info("Finished data pack reloading successfully on {}.", Thread.currentThread());
			} else {
				LOGGER.error("Failed to reload on {} because {}.", Thread.currentThread(), error);
			}
		});
	}
}
