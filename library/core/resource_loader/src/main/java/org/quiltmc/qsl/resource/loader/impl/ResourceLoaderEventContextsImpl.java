/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.resource.loader.impl;

import java.lang.ref.WeakReference;
import java.util.Optional;
import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;

import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;

@ApiStatus.Internal
public final class ResourceLoaderEventContextsImpl {
	public static WeakReference<MinecraftServer> server;

	public static class ReloadStartContext implements ResourceLoaderEvents.StartDataPackReload.Context {
		private final Supplier<ResourceManager> resourceManager;
		private final ResourceManager previousResourceManager;

		public ReloadStartContext(Supplier<ResourceManager> resourceManager, ResourceManager previousResourceManager) {
			this.resourceManager = resourceManager;
			this.previousResourceManager = previousResourceManager;
		}

		@Override
		public MinecraftServer server() {
			return server != null ? server.get() : null;
		}

		@Override
		public ResourceManager resourceManager() {
			return this.resourceManager.get();
		}

		@Override
		public Optional<ResourceManager> previousResourceManager() {
			return Optional.ofNullable(this.previousResourceManager);
		}
	}

	public record ReloadEndContext(
			ResourceManager resourceManager, DynamicRegistryManager dynamicRegistries, Optional<Throwable> error
	) implements ResourceLoaderEvents.EndDataPackReload.Context {
		@Override
		public MinecraftServer server() {
			return server != null ? server.get() : null;
		}
	}
}
