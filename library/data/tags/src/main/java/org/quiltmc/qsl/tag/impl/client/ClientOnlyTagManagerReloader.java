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

package org.quiltmc.qsl.tag.impl.client;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.resource.ResourceManager;
import net.minecraft.registry.tag.TagGroupLoader;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.resource.loader.api.reloader.SimpleResourceReloader;

@ClientOnly
@ApiStatus.Internal
class ClientOnlyTagManagerReloader implements SimpleResourceReloader<List<ClientOnlyTagManagerReloader.Entry>> {
	private static final Identifier ID = new Identifier(ClientQuiltTagsMod.NAMESPACE, "client_only_tags");

	@Override
	public @NotNull Identifier getQuiltId() {
		return ID;
	}

	@Override
	public CompletableFuture<List<Entry>> load(ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			var entries = new ArrayList<Entry>();

			ClientTagRegistryManager.forEach(clientTagRegistryManager -> {
				entries.add(new Entry(clientTagRegistryManager, clientTagRegistryManager.load(manager)));
			});

			return entries;
		}, executor);
	}

	@Override
	public CompletableFuture<Void> apply(List<Entry> data, ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			data.forEach(entry -> entry.manager().setSerializedTags(entry.serializedTags()));
		}, executor);
	}

	protected record Entry(ClientTagRegistryManager<?> manager,
	                       Map<Identifier, List<TagGroupLoader.EntryWithSource>> serializedTags) {
	}
}
