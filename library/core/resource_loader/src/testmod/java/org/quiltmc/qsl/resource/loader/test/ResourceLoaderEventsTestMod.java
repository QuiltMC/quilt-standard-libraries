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

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;

import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;

public class ResourceLoaderEventsTestMod implements ResourceLoaderEvents.StartDataPackReload,
		ResourceLoaderEvents.EndDataPackReload {
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceLoaderEventsTestMod.class);

	@Override
	public void onStartDataPackReload(@Nullable MinecraftServer server, @Nullable ServerResourceManager oldResourceManager) {
		LOGGER.info("Preparing for data pack reload, old resource manager: {}", oldResourceManager);
	}

	@Override
	public void onEndDataPackReload(@Nullable MinecraftServer server, ServerResourceManager resourceManager, @Nullable Throwable error) {
		if (error == null) {
			LOGGER.info("Finished data pack reloading successfully on {}.", Thread.currentThread());
		} else {
			LOGGER.error("Failed to reload on {} because {}.", Thread.currentThread(), error);
		}
	}
}
