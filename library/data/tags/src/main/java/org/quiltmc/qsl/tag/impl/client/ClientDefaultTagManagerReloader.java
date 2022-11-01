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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.DefaultClientResourcePack;
import net.minecraft.resource.AutoCloseableResourceManager;
import net.minecraft.resource.MultiPackResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePackManager;
import net.minecraft.resource.pack.ResourcePackProfile;
import net.minecraft.resource.pack.ResourcePackSource;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import org.quiltmc.qsl.resource.loader.impl.ModResourcePackProvider;
import org.quiltmc.qsl.resource.loader.impl.QuiltMultiPackResourceManagerHooks;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;
import org.quiltmc.qsl.tag.mixin.client.ClientBuiltinResourcePackProviderAccessor;
import org.quiltmc.qsl.tag.mixin.client.DefaultClientResourcePackAccessor;

@Environment(EnvType.CLIENT)
@ApiStatus.Internal
final class ClientDefaultTagManagerReloader extends ClientOnlyTagManagerReloader {
	private static final Identifier ID = new Identifier(ClientQuiltTagsMod.NAMESPACE, "client_default_tags");
	private final ResourcePackManager resourcePackManager;

	ClientDefaultTagManagerReloader() {
		var defaultPack = ((ClientBuiltinResourcePackProviderAccessor) MinecraftClient.getInstance().getResourcePackProvider())
				.getPack();

		var pack = ResourceLoaderImpl.buildMinecraftResourcePack(ResourceType.SERVER_DATA, new DefaultClientResourcePack(
				ClientBuiltinResourcePackProviderAccessor.getDefaultPackMetadata(), ((DefaultClientResourcePackAccessor) defaultPack).getIndex()
		));
		this.resourcePackManager = new ResourcePackManager(ResourceType.SERVER_DATA, (profileAdder, factory) -> {
			profileAdder.accept(factory.create("vanilla", pack.getDisplayName(), true, () -> pack,
					ClientBuiltinResourcePackProviderAccessor.getDefaultPackMetadata(), ResourcePackProfile.InsertionPosition.BOTTOM,
					ResourcePackSource.PACK_SOURCE_BUILTIN)
			);
		}, ModResourcePackProvider.SERVER_RESOURCE_PACK_PROVIDER);
	}

	@Override
	public @NotNull Identifier getQuiltId() {
		return ID;
	}

	/**
	 * Returns a resource manager with a modified resource type but with the same resource packs as the client.
	 *
	 * @return the modified resource manager
	 */
	private AutoCloseableResourceManager getServerDataResourceManager() {
		resourcePackManager.scanPacks();
		var manager = new MultiPackResourceManager(ResourceType.SERVER_DATA, resourcePackManager.createResourcePacks());
		((QuiltMultiPackResourceManagerHooks) manager).quilt$appendTopPacks();
		return manager;
	}

	@Override
	public CompletableFuture<List<Entry>> load(ResourceManager manager, Profiler profiler, Executor executor) {
		// First we need to transform the resource manager into one with the type SERVER_DATA,
		// then we can continue as normal.
		return CompletableFuture.supplyAsync(this::getServerDataResourceManager, executor)
				.thenComposeAsync(resourceManager -> super.load(resourceManager, profiler, executor)
								.whenComplete((entries, throwable) -> resourceManager.close()),
						executor
				);
	}

	@Override
	public CompletableFuture<Void> apply(List<Entry> data, ResourceManager manager, Profiler profiler, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			data.forEach(entry -> entry.manager().setFallbackSerializedTags(entry.serializedTags()));
		}, executor);
	}
}
