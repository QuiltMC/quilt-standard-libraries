/*
 * Copyright 2022 The Quilt Project
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

import java.util.List;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.resource.MultiPackResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePack;

import org.quiltmc.qsl.resource.loader.api.ResourcePackRegistrationContext;

@ApiStatus.Internal
final class ResourcePackRegistrationContextImpl implements ResourcePackRegistrationContext {
	private final MultiPackResourceManager resourceManager;
	private final Consumer<ResourcePack> packConsumer;

	ResourcePackRegistrationContextImpl(MultiPackResourceManager resourceManager, Consumer<ResourcePack> packConsumer) {
		this.resourceManager = resourceManager;
		this.packConsumer = packConsumer;
	}

	ResourcePackRegistrationContextImpl(ResourceType type, List<ResourcePack> packs, Consumer<ResourcePack> packConsumer) {
		this(new MultiPackResourceManager(type, packs), packConsumer);
	}

	@Override
	public @NotNull ResourceManager resourceManager() {
		return this.resourceManager;
	}

	@Override
	public void addResourcePack(@NotNull ResourcePack pack) {
		ResourceLoaderImpl.flattenPacks(pack, this.packConsumer);
		((QuiltMultiPackResourceManagerHooks) this.resourceManager).quilt$recomputeNamespaces();
	}
}
