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

package org.quiltmc.qsl.resource.loader.test.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.quiltmc.qsl.resource.loader.api.client.ClientResourceLoaderEvents;
import org.quiltmc.qsl.resource.loader.test.ResourceLoaderTestMod;

public class ClientResourceLoaderEventsTestMod implements ClientResourceLoaderEvents.StartResourcePackReload,
		ClientResourceLoaderEvents.EndResourcePackReload {
	private static final Logger LOGGER = LoggerFactory.getLogger(ClientResourceLoaderEventsTestMod.class);
	private long start;

	@Override
	public void onStartResourcePackReload(ClientResourceLoaderEvents.StartResourcePackReload.Context context) {
		LOGGER.info("Preparing for resource pack reload, resource manager: {}. Is it the first time?: {}",
				context.resourceManager(), context.isFirst());

		ResourceLoaderTestMod.loadingClientResources = true;
		this.start = System.currentTimeMillis();
	}

	@Override
	public void onEndResourcePackReload(ClientResourceLoaderEvents.EndResourcePackReload.Context context) {
		LOGGER.info("Took {}ms to perform resource pack reload.", (System.currentTimeMillis() - this.start));

		context.error().ifPresentOrElse(
				error -> LOGGER.error("Failed to reload resource packs on {} because {}.", Thread.currentThread(), error),
				() -> LOGGER.info("Finished {}resource pack reloading successfully on {}.", (context.isFirst() ? "first " : ""), Thread.currentThread())
		);
	}
}
