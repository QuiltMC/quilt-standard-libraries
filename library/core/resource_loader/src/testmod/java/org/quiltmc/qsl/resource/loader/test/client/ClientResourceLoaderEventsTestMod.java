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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;

import org.quiltmc.qsl.resource.loader.api.client.ClientResourceLoaderEvents;

public class ClientResourceLoaderEventsTestMod implements ClientResourceLoaderEvents.StartResourcePackReload,
		ClientResourceLoaderEvents.EndResourcePackReload {
	private static final Logger LOGGER = LogManager.getLogger();

	@Override
	public void onStartResourcePackReload(MinecraftClient client, ResourceManager resourceManager, boolean first) {
		LOGGER.info("Preparing for resource pack reload, resource manager: {}. Is it the first time?: {}",
				resourceManager, first);
	}

	@Override
	public void onEndResourcePackReload(MinecraftClient client, ResourceManager resourceManager, boolean first, @Nullable Throwable error) {
		if (error == null) {
			LOGGER.info("Finished {}resource pack reloading successfully on {}.",
					(first ? "first " : ""), Thread.currentThread());
		} else {
			LOGGER.error("Failed to reload resource packs on {} because {}.", Thread.currentThread(), error);
		}
	}
}
