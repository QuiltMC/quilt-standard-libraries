/*
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.qsl.tag.impl.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.resource.MultiPackResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
final class ClientDefaultTagManagerReloader extends ClientOnlyTagManagerReloader {
	private static final Identifier ID = new Identifier(ClientQuiltTagsMod.NAMESPACE, "client_default_tags");

	@Override
	public Identifier getQuiltId() {
		return ID;
	}

	/**
	 * Returns a resource manager with a modified resource type but with the same resource packs as the client.
	 * <p>
	 * Flaw: if a resource pack which can be loaded by client but only has server data won't be added to this resource manager as not present in client resource manager.
	 *
	 * @param base the client vanilla resource manager
	 * @return the modified resource manager
	 */
	private static ResourceManager getServerDataResourceManager(ResourceManager base) {
		return new MultiPackResourceManager(ResourceType.SERVER_DATA, base.streamResourcePacks()
				.filter(resourcePack -> !resourcePack.getNamespaces(ResourceType.SERVER_DATA).isEmpty())
				.collect(Collectors.toList()));
	}

	@Override
	public CompletableFuture<List<Entry>> load(ResourceManager manager, Profiler profiler, Executor executor) {
		// First we need to transform the resource manager into one with the type SERVER_DATA,
		// then we can continue as normal.
		return CompletableFuture.supplyAsync(() -> getServerDataResourceManager(manager), executor)
				.thenComposeAsync(resourceManager -> super.load(resourceManager, profiler, executor), executor);
	}

	@Override
	public CompletableFuture<Void> apply(List<Entry> data, ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			data.forEach(entry -> entry.manager().setFallbackSerializedTags(entry.serializedTags()));
		}, executor);
	}
}
