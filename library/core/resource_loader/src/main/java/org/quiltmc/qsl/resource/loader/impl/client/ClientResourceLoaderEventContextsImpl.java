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

package org.quiltmc.qsl.resource.loader.impl.client;

import java.util.Optional;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resource.ResourceManager;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.resource.loader.api.client.ClientResourceLoaderEvents;

@ClientOnly
@ApiStatus.Internal
public class ClientResourceLoaderEventContextsImpl implements ClientResourceLoaderEvents.StartResourcePackReload.Context {
	private final ResourceManager resourceManager;
	private final boolean first;

	public ClientResourceLoaderEventContextsImpl(ResourceManager resourceManager, boolean first) {
		this.resourceManager = resourceManager;
		this.first = first;
	}

	@Override
	public ResourceManager resourceManager() {
		return this.resourceManager;
	}

	@Override
	public boolean isFirst() {
		return this.first;
	}

	public static final class ReloadEndContext extends ClientResourceLoaderEventContextsImpl
			implements ClientResourceLoaderEvents.EndResourcePackReload.Context {
		private final Optional<Throwable> error;

		public ReloadEndContext(ResourceManager resourceManager, boolean first, Optional<Throwable> error) {
			super(resourceManager, first);
			this.error = error;
		}

		@Override
		public Optional<Throwable> error() {
			return this.error;
		}
	}
}
