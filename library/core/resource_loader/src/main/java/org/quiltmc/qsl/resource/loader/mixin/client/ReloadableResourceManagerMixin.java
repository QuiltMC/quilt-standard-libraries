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

package org.quiltmc.qsl.resource.loader.mixin.client;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceReload;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.util.Unit;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.resource.loader.api.client.ClientResourceLoaderEvents;
import org.quiltmc.qsl.resource.loader.impl.client.ClientResourceLoaderEventContextsImpl;
import org.quiltmc.qsl.resource.loader.impl.client.ClientResourceLoaderImpl;

@ClientOnly
@Mixin(ReloadableResourceManager.class)
public abstract class ReloadableResourceManagerMixin implements ResourceManager {
	@Inject(
			method = "reload",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resource/SimpleResourceReload;create(Lnet/minecraft/resource/ResourceManager;Ljava/util/List;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Z)Lnet/minecraft/resource/ResourceReload;"
			)
	)
	private void reload(Executor prepareExecutor, Executor applyExecutor, CompletableFuture<Unit> initialStage,
			List<ResourcePack> packs, CallbackInfoReturnable<ResourceReload> info) {
		Boolean firstReload = ClientResourceLoaderImpl.getReloadContext();

		if (firstReload != null) {
			try {
				ClientResourceLoaderEvents.START_RESOURCE_PACK_RELOAD.invoker().onStartResourcePackReload(
						new ClientResourceLoaderEventContextsImpl(this, firstReload)
				);
			} finally {
				ClientResourceLoaderImpl.popReloadContext();
			}
		}
	}
}
