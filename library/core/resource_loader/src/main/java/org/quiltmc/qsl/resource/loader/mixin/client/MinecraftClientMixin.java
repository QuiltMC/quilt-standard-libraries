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

package org.quiltmc.qsl.resource.loader.mixin.client;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Function;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.RunArgs;
import net.minecraft.resource.ReloadableResourceManager;
import net.minecraft.server.WorldStem;
import net.minecraft.world.level.storage.LevelStorage;

import org.quiltmc.qsl.base.api.util.TriState;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.quiltmc.qsl.resource.loader.api.client.ClientResourceLoaderEvents;

@Environment(EnvType.CLIENT)
@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
	@Unique
	private static final String START_INTEGRATED_SERVER_METHOD = "startIntegratedServer(" +
			"Ljava/lang/String;Ljava/util/function/Function;Ljava/util/function/Function;" +
			"ZLnet/minecraft/client/MinecraftClient$WorldLoadAction;)V";
	@Unique
	private static final TriState EXPERIMENTAL_SCREEN_OVERRIDE = TriState.fromProperty("quilt.resource_loader.experimental_screen_override");

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
				(MinecraftClient) (Object) this, this.resourceManager, true
		);
	}

	// Lambda method in MinecraftClient#<init>, at MinecraftClient#setOverlay.
	// Take an Optional<Throwable> parameter.
	@SuppressWarnings("target")
	@Inject(method = "m_aaltpyph(Ljava/util/Optional;)V", at = @At("HEAD"))
	private void onFirstEndReloadResources(Optional<Throwable> error, CallbackInfo ci) {
		ClientResourceLoaderEvents.END_RESOURCE_PACK_RELOAD.invoker().onEndResourcePackReload(
				(MinecraftClient) (Object) this, this.resourceManager, true, error.orElse(null)
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
				(MinecraftClient) (Object) this, this.resourceManager, false
		);
	}

	// Lambda method in MinecraftClient#reloadResources, at MinecraftClient#setOverlay.
	// Take an Optional<Throwable> parameter.
	@SuppressWarnings("target")
	@Inject(method = "method_24228(Ljava/util/concurrent/CompletableFuture;Ljava/util/Optional;)V", at = @At(value = "HEAD"))
	private void onEndReloadResources(CompletableFuture<Void> completableFuture, Optional<Throwable> error, CallbackInfo ci) {
		ClientResourceLoaderEvents.END_RESOURCE_PACK_RELOAD.invoker().onEndResourcePackReload(
				(MinecraftClient) (Object) this, this.resourceManager, false, error.orElse(null)
		);
	}

	@Inject(
			method = START_INTEGRATED_SERVER_METHOD,
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/MinecraftClient;createWorldStem(Lnet/minecraft/resource/pack/ResourcePackManager;ZLnet/minecraft/server/WorldStem$Supplier;Lnet/minecraft/server/WorldStem$WorldDataSupplier;)Lnet/minecraft/server/WorldStem;"
			)
	)
	private void onStartDataPackReloading(String worldName,
			Function<LevelStorage.Session, WorldStem.Supplier> dataPackSettingsGetter,
			Function<LevelStorage.Session, WorldStem.WorldDataSupplier> worldDataGetter,
			boolean safeMode, @Coerce Object worldLoadAction, CallbackInfo ci) {
		ResourceLoaderEvents.START_DATA_PACK_RELOAD.invoker().onStartDataPackReload(null, null);
	}

	@ModifyVariable(method = START_INTEGRATED_SERVER_METHOD, at = @At(value = "STORE", ordinal = 0))
	private WorldStem onSuccessfulDataPackReloading(WorldStem resources) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, resources.resourceManager(), null);
		return resources; // noop
	}

	@ModifyArg(
			method = START_INTEGRATED_SERVER_METHOD,
			at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Throwable;)V", remap = false, ordinal = 0),
			index = 1
	)
	private Throwable onFailedDataPackReloading(Throwable throwable) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, null, throwable);
		return throwable; // noop
	}

	@ModifyVariable(
			method = START_INTEGRATED_SERVER_METHOD,
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/MinecraftClient$WorldLoadAction;NONE:Lnet/minecraft/client/MinecraftClient$WorldLoadAction;",
					ordinal = 0
			),
			ordinal = 2,
			index = 11,
			name = "bl2",
			require = 0
	)
	private boolean replaceIsExperimental(boolean isExperimental) {
		if (EXPERIMENTAL_SCREEN_OVERRIDE.toBooleanOrElse(true)) return false;
		return isExperimental;
	}
}
