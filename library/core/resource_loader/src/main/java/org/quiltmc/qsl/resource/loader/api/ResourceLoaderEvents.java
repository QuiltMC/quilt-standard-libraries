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

package org.quiltmc.qsl.resource.loader.api;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;

/**
 * Events related to the resource loader.
 */
public final class ResourceLoaderEvents {
	private ResourceLoaderEvents() {
		throw new UnsupportedOperationException("ResourceLoaderEvents only contains static definitions.");
	}

	/**
	 * An event indicating the start of the reloading of data packs on a Minecraft server.
	 * <p>
	 * This event should not be used to load resources, use {@link ResourceLoader#registerReloader(IdentifiableResourceReloader)} instead.
	 */
	public static final Event<StartDataPackReload> START_DATA_PACK_RELOAD = Event.create(StartDataPackReload.class,
			callbacks -> (server, resourceManager) -> {
				for (var callback : callbacks) {
					callback.onStartDataPackReload(server, resourceManager);
				}
			});

	/**
	 * An event indicating the end of the reloading of data packs on a Minecraft server.
	 * <p>
	 * This event should not be used to load resources, use {@link ResourceLoader#registerReloader(IdentifiableResourceReloader)} instead.
	 */
	public static final Event<EndDataPackReload> END_DATA_PACK_RELOAD = Event.create(EndDataPackReload.class,
			callbacks -> (server, resourceManager, error) -> {
				for (var callback : callbacks) {
					callback.onEndDataPackReload(server, resourceManager, error);
				}
			});

	/**
	 * Functional interface to be implemented on callbacks for {@link #START_DATA_PACK_RELOAD}.
	 *
	 * @see #START_DATA_PACK_RELOAD
	 */
	@FunctionalInterface
	public interface StartDataPackReload extends EventAwareListener {
		/**
		 * Called before data packs on a Minecraft server have been reloaded.
		 *
		 * @param server             the server, may be {@code null} for the first reload
		 * @param oldResourceManager the old resource manager, to be replaced, may be {@code null} for the first reload
		 */
		void onStartDataPackReload(@Nullable MinecraftServer server, @Nullable ResourceManager oldResourceManager);
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #END_DATA_PACK_RELOAD}.
	 *
	 * @see #END_DATA_PACK_RELOAD
	 */
	@FunctionalInterface
	public interface EndDataPackReload extends EventAwareListener {
		/**
		 * Called after data packs on a Minecraft server have been reloaded.
		 * <p>
		 * If the reload was not successful, the old data packs will be kept.
		 *
		 * @param server          the server, may be {@code null} for the first reload
		 * @param resourceManager the resource manager, may be {@code null} if the data pack reload failed
		 * @param error           present if the data pack reload failed, or {@code null} otherwise
		 */
		void onEndDataPackReload(@Nullable MinecraftServer server, ResourceManager resourceManager, @Nullable Throwable error);
	}
}
