/*
 * Copyright 2021 QuiltMC
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
import java.util.function.Function;

import com.mojang.datafixers.util.Function4;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.resource.DataPackSettings;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourcePackManager;
import net.minecraft.resource.ServerResourceManager;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.storage.LevelStorage;

import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.quiltmc.qsl.resource.loader.api.client.ClientResourceLoaderEvents;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Shadow
	@Final
	private ReloadableResourceManager resourceManager;

	@Inject(
			method = "<init>",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/MinecraftClient;setOverlay(Lnet/minecraft/client/gui/screen/Overlay;)V"
			)
	)
	private void onFirstReloadResources(RunArgs runArgs, CallbackInfo ci) {
		ClientResourceLoaderEvents.START_RESOURCE_PACK_RELOAD.invoker().onStartResourcePackReload(
				(MinecraftClient) (Object) this, this.resourceManager
		);
	}

	// Lambda method in MinecraftClient#<init>, at MinecraftClient#setOverlay.
	// Take an Optional<Throwable> parameter.
	@Inject(method = "m_aaltpyph(Ljava/util/Optional;)V", at = @At("HEAD"))
	private void onFirstEndReloadResources(Optional<Throwable> error, CallbackInfo ci) {
		ClientResourceLoaderEvents.END_RESOURCE_PACK_RELOAD.invoker().onEndResourcePackReload(
				(MinecraftClient) (Object) this, this.resourceManager, error
		);
	}

	@Inject(
			method = "reloadResources(Z)Ljava/util/concurrent/CompletableFuture;",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/MinecraftClient;setOverlay(Lnet/minecraft/client/gui/screen/Overlay;)V"
			)
	)
	private void onStartReloadResources(boolean force, CallbackInfoReturnable<CompletableFuture<Void>> cir) {
		ClientResourceLoaderEvents.START_RESOURCE_PACK_RELOAD.invoker().onStartResourcePackReload(
				(MinecraftClient) (Object) this, this.resourceManager
		);
	}

	// Lambda method in MinecraftClient#reloadResources, at MinecraftClient#setOverlay.
	// Take an Optional<Throwable> parameter.
	@Inject(method = "m_pxfxqhcl(Ljava/util/concurrent/CompletableFuture;Ljava/util/Optional;)V", at = @At(value = "HEAD"))
	private void onEndReloadResources(CompletableFuture<Void> completableFuture, Optional<Throwable> error, CallbackInfo ci) {
		ClientResourceLoaderEvents.END_RESOURCE_PACK_RELOAD.invoker().onEndResourcePackReload(
				(MinecraftClient) (Object) this, this.resourceManager, error
		);
	}

	@Inject(
			method = "createIntegratedResourceManager",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/resource/ServerResourceManager;reload(Ljava/util/List;Lnet/minecraft/util/registry/DynamicRegistryManager;Lnet/minecraft/server/command/CommandManager$RegistrationEnvironment;ILjava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
			)
	)
	private void onStartDataPackReloading(DynamicRegistryManager.Impl registryManager,
	                                      Function<LevelStorage.Session, DataPackSettings> dataPackSettingsGetter,
	                                      Function4<LevelStorage.Session, DynamicRegistryManager.Impl, ResourceManager, DataPackSettings, SaveProperties> savePropertiesGetter,
	                                      boolean safeMode, LevelStorage.Session storageSession,
	                                      CallbackInfoReturnable<MinecraftClient.IntegratedResourceManager> cir) {
		ResourceLoaderEvents.START_DATA_PACK_RELOAD.invoker().onStartDataPackReload(null, null);
	}

	@ModifyVariable(method = "createIntegratedResourceManager", at = @At(value = "STORE", ordinal = 0))
	private ServerResourceManager onSuccessfulDataPackReloading(ServerResourceManager resourceManager) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, resourceManager, null);
		return resourceManager; // noop
	}

	@Inject(
			method = "createIntegratedResourceManager",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/resource/ResourcePackManager;close()V"),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onFailedDataPackReloading(DynamicRegistryManager.Impl registryManager,
	                                       Function<LevelStorage.Session, DataPackSettings> dataPackSettingsGetter,
	                                       Function4<LevelStorage.Session, DynamicRegistryManager.Impl, ResourceManager, DataPackSettings, SaveProperties> savePropertiesGetter,
	                                       boolean safeMode, LevelStorage.Session storageSession,
	                                       CallbackInfoReturnable<MinecraftClient.IntegratedResourceManager> cir,
	                                       DataPackSettings dataPackSettings,
	                                       ResourcePackManager resourcePackManager,
	                                       Exception exception) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, null, exception);
	}
}
