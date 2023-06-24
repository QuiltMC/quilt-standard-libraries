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

package org.quiltmc.qsl.resource.loader.mixin;

import java.lang.ref.WeakReference;
import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

import com.google.common.collect.ImmutableList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.resource.AutoCloseableResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.server.MinecraftServer;

import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderEventContextsImpl;

@Mixin(MinecraftServer.class)
public abstract class MinecraftServerMixin {
	@Shadow
	public abstract ResourceManager getResourceManager();

	@Shadow
	public abstract DynamicRegistryManager.Frozen getRegistryManager();

	@Inject(
			method = "method_29437(Lnet/minecraft/registry/DynamicRegistryManager$Frozen;Lcom/google/common/collect/ImmutableList;)Ljava/util/concurrent/CompletionStage;",
			at = @At("RETURN"),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onReloadResourcesStart(
			DynamicRegistryManager.Frozen frozen, ImmutableList packs, CallbackInfoReturnable<CompletionStage> cir,
			AutoCloseableResourceManager currentResourceManager
	) {
		ResourceLoaderEventContextsImpl.server = new WeakReference<>((MinecraftServer) (Object) this);
		ResourceLoaderEvents.START_DATA_PACK_RELOAD.invoker().onStartDataPackReload(new ResourceLoaderEventContextsImpl.ReloadStartContext(
				() -> currentResourceManager, this.getResourceManager()
		));
	}

	@Inject(method = "reloadResources", at = @At("TAIL"))
	private void onReloadResourcesEnd(Collection<String> collection, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		cir.getReturnValue().handleAsync((value, throwable) -> {
			ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(new ResourceLoaderEventContextsImpl.ReloadEndContext(
					this.getResourceManager(), this.getRegistryManager(), Optional.ofNullable(throwable)
			));
			return value;
		}, (MinecraftServer) (Object) this);
	}
}
