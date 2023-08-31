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

package org.quiltmc.qsl.resource.loader.mixin.client;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.resource.ReloadableResourceManager;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.resource.loader.api.client.ClientResourceLoaderEvents;
import org.quiltmc.qsl.resource.loader.impl.client.ClientResourceLoaderEventContextsImpl;
import org.quiltmc.qsl.resource.loader.impl.client.ClientResourceLoaderImpl;

@ClientOnly
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Shadow
	@Final
	private ReloadableResourceManager resourceManager;

	@Inject(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resource/ReloadableResourceManager;reload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/util/List;)Lnet/minecraft/resource/ResourceReload;"
			)
	)
	private void onFirstReloadResources(RunArgs runArgs, CallbackInfo ci) {
		ClientResourceLoaderImpl.pushReloadContext(true);
	}

	// Lambda method in MinecraftClient#<init>, at MinecraftClient#setOverlay.
	// Take an Optional<Throwable> parameter.
	@SuppressWarnings("target")
	@Inject(method = "method_24040(Ljava/util/Optional;)V", at = @At("HEAD"))
	private void onFirstEndReloadResources(Optional<Throwable> error, CallbackInfo ci) {
		ClientResourceLoaderEvents.END_RESOURCE_PACK_RELOAD.invoker().onEndResourcePackReload(
				new ClientResourceLoaderEventContextsImpl.ReloadEndContext(this.resourceManager, true, error)
		);
	}

	@Inject(
			method = "reloadResources(Z)Ljava/util/concurrent/CompletableFuture;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resource/ReloadableResourceManager;reload(Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;Ljava/util/concurrent/CompletableFuture;Ljava/util/List;)Lnet/minecraft/resource/ResourceReload;"
			)
	)
	private void onStartReloadResources(boolean force, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		ClientResourceLoaderImpl.pushReloadContext(false);
	}

	// Lambda method in MinecraftClient#reloadResources, at MinecraftClient#setOverlay.
	// Take an Optional<Throwable> parameter.
	@SuppressWarnings("target")
	@Inject(method = "method_24228(ZLjava/util/concurrent/CompletableFuture;Ljava/util/Optional;)V", at = @At(value = "HEAD"))
	private void onEndReloadResources(boolean force, CompletableFuture<Void> completableFuture, Optional<Throwable> error, CallbackInfo ci) {
		ClientResourceLoaderEvents.END_RESOURCE_PACK_RELOAD.invoker().onEndResourcePackReload(
				new ClientResourceLoaderEventContextsImpl.ReloadEndContext(this.resourceManager, false, error)
		);
	}
}
