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

package org.quiltmc.qsl.resource.loader.test.client;

import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import org.quiltmc.qsl.resource.loader.api.client.ClientResourceLoaderEvents;

public class ClientResourceLoaderEventsTestMod implements ClientModInitializer {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onInitializeClient() {
		ClientResourceLoaderEvents.START_RESOURCE_PACK_RELOAD.register((client, resourceManager) ->
				LOGGER.info("Preparing for resource pack reload, resource manager: {}", resourceManager));
		ClientResourceLoaderEvents.END_RESOURCE_PACK_RELOAD.register((server, resourceManager, error) -> {
			if (error == null) {
				LOGGER.info("Finished resource pack reloading successfully on {}.", Thread.currentThread());
			} else {
				LOGGER.error("Failed to reload resource packs on {} because {}.", Thread.currentThread(), error);
			}
		});
	}
}
