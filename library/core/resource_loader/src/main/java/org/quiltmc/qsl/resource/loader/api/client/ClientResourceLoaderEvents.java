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

package org.quiltmc.qsl.resource.loader.api.client;

import java.util.Optional;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.client.ClientEventAwareListener;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;

/**
 * Events related to the resource loader of the Minecraft client.
 */
@ClientOnly
public final class ClientResourceLoaderEvents {
	private ClientResourceLoaderEvents() {
		throw new UnsupportedOperationException("ClientResourceLoaderEvents only contains static definitions.");
	}

	/**
	 * An event indicating the start of the reloading of resource packs on the Minecraft client.
	 * <p>
	 * This event should not be used to load resources, use {@link ResourceLoader#registerReloader(IdentifiableResourceReloader)} instead.
	 */
	public static final Event<StartResourcePackReload> START_RESOURCE_PACK_RELOAD = Event.create(StartResourcePackReload.class,
			callbacks -> context -> {
				for (var callback : callbacks) {
					callback.onStartResourcePackReload(context);
				}
			});

	/**
	 * An event indicating the end of the reloading of resource packs on the Minecraft client.
	 * <p>
	 * This event should not be used to load resources, use {@link ResourceLoader#registerReloader(IdentifiableResourceReloader)} instead.
	 */
	public static final Event<EndResourcePackReload> END_RESOURCE_PACK_RELOAD = Event.create(EndResourcePackReload.class,
			callbacks -> context -> {
				for (var callback : callbacks) {
					callback.onEndResourcePackReload(context);
				}
			});

	/**
	 * Functional interface to be implemented on callbacks for {@link #START_RESOURCE_PACK_RELOAD}.
	 *
	 * @see #START_RESOURCE_PACK_RELOAD
	 */
	@FunctionalInterface
	public interface StartResourcePackReload extends ClientEventAwareListener {
		/**
		 * Called before resource packs on the Minecraft client have been reloaded.
		 *
		 * @param context the resource reload context
		 */
		void onStartResourcePackReload(Context context);

		@ApiStatus.NonExtendable
		interface Context {
			/**
			 * {@return the client instance}
			 */
			@Contract(pure = true)
			default MinecraftClient client() {
				return MinecraftClient.getInstance();
			}

			/**
			 * {@return the resource manager}
			 */
			@Contract(pure = true)
			ResourceManager resourceManager();

			/**
			 * Gets whether the resource reload is the first or not.
			 *
			 * @return {@code true} if it's the first resource reload, or {@code false} otherwise
			 */
			@Contract(pure = true)
			boolean isFirst();
		}
	}

	/**
	 * Functional interface to be implemented on callbacks for {@link #END_RESOURCE_PACK_RELOAD}.
	 *
	 * @see #END_RESOURCE_PACK_RELOAD
	 */
	@FunctionalInterface
	public interface EndResourcePackReload extends ClientEventAwareListener {
		/**
		 * Called after resource packs on the Minecraft client have been reloaded.
		 * <p>
		 * If the reload was not successful, the old resource packs will be kept.
		 *
		 * @param context the resource reload context
		 */
		void onEndResourcePackReload(Context context);

		@ApiStatus.NonExtendable
		interface Context extends StartResourcePackReload.Context {
			/**
			 * {@return present if the resource pack reload failed, or {@linkplain Optional#empty() empty} otherwise}
			 */
			@Contract(pure = true)
			Optional<Throwable> error();
		}
	}
}
