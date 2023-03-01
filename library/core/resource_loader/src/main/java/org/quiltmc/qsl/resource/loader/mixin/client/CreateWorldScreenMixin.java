/*
 * Copyright 2021-2023 QuiltMC
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

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.world.WorldCreationContext;
import net.minecraft.feature_flags.FeatureFlags;
import net.minecraft.resource.AutoCloseableResourceManager;
import net.minecraft.resource.pack.ResourcePackManager;
import net.minecraft.server.ServerReloadableResources;
import net.minecraft.server.world.FeatureAndDataSettings;
import net.minecraft.registry.LayeredRegistryManager;
import net.minecraft.unmapped.C_hypraqfe;
import net.minecraft.util.Unit;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

@ClientOnly
@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {
	@Shadow
	@Final
	C_hypraqfe f_rsmdhran;

	@Dynamic
	@Inject(
			method = "m_qcsfhvrb(Lnet/minecraft/resource/AutoCloseableResourceManager;Lnet/minecraft/server/ServerReloadableResources;Lnet/minecraft/registry/LayeredRegistryManager;Lnet/minecraft/client/gui/screen/world/CreateWorldScreen$C_mxqwwbun;)Lnet/minecraft/client/world/WorldCreationContext;",
			at = @At("HEAD")
	)
	private static void onEndDataPackLoadOnOpen(AutoCloseableResourceManager resourceManager, ServerReloadableResources resources,
			LayeredRegistryManager<?> layeredRegistryManager, @Coerce Object c_mxqwwbun, CallbackInfoReturnable<WorldCreationContext> cir) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, resourceManager, null);
	}

	@Inject(
			method = "m_btwtdkmu",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/WorldLoader;load(Lnet/minecraft/server/WorldLoader$InitConfig;Lnet/minecraft/server/WorldLoader$LoadContextSupplier;Lnet/minecraft/server/WorldLoader$ApplierFactory;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
			)
	)
	private void onDataPackLoadStart(
			ResourcePackManager resourcePackManager, FeatureAndDataSettings featureAndDataSettings, Consumer<FeatureAndDataSettings> consumer,
			CallbackInfo ci
	) {
		ResourceLoaderEvents.START_DATA_PACK_RELOAD.invoker().onStartDataPackReload(null, null);
	}

	@Inject(
			method = "open",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/WorldLoader;load(Lnet/minecraft/server/WorldLoader$InitConfig;Lnet/minecraft/server/WorldLoader$LoadContextSupplier;Lnet/minecraft/server/WorldLoader$ApplierFactory;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
			)
	)
	private static void onDataPackLoadStart(MinecraftClient minecraftClient, Screen screen, CallbackInfo ci) {
		ResourceLoaderEvents.START_DATA_PACK_RELOAD.invoker().onStartDataPackReload(null, null);
	}

	// Lambda method in CreateWorldScreen#m_btwtdkmu, search for a resource manager being closed.
	// Inject before closing the resource manager.
	@Dynamic
	@Inject(slice = @Slice(to = @At(value = "INVOKE", target = "Lnet/minecraft/resource/AutoCloseableResourceManager;close()V")),
			method = "m_qcsfhvrb(Lnet/minecraft/resource/AutoCloseableResourceManager;Lnet/minecraft/server/ServerReloadableResources;Lnet/minecraft/registry/LayeredRegistryManager;Lnet/minecraft/client/gui/screen/world/CreateWorldScreen$C_mxqwwbun;)Lnet/minecraft/client/world/WorldCreationContext",
			at = @At("HEAD")
	)
	private static void onCreateDataPackLoadEnd(AutoCloseableResourceManager resourceManager, ServerReloadableResources resources,
			LayeredRegistryManager<?> layeredRegistryManager, @Coerce Object c_mxqwwbun, CallbackInfoReturnable<WorldCreationContext> cir) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, resourceManager, null);
	}

	// Lambda method in CreateWorldScreen#m_btwtdkmu, passed CompletableFuture#handle.
	// Take Void and Throwable parameters.
	@Inject(
			slice = @Slice(to = @At(value = "CONSTANT", args = "stringValue=dataPack.validation.failed")),
			method = "m_fiszvdug(Ljava/util/function/Consumer;Ljava/lang/Void;Ljava/lang/Throwable;)Ljava/lang/Object;",
			at = @At(
					value = "INVOKE",
					target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Throwable;)V",
					shift = At.Shift.AFTER,
					remap = false
			)
	)
	private void onFailDataPackLoading(
			Consumer<FeatureAndDataSettings> consumer, Void unused, Throwable throwable, CallbackInfoReturnable<Object> cir
	) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, null, throwable);
	}

	@Inject(method = "createLevel", at = @At("HEAD"))
	private void onCreateLevelStart(CallbackInfo ci) {
		if (FeatureFlags.containsDefault(this.f_rsmdhran.m_xkfzagsg().dataConfiguration().enabledFeatures())) {
			ResourceLoaderImpl.EXPERIMENTAL_FEATURES_ENABLED.set(Unit.INSTANCE);
		}
	}

	@Inject(method = "createLevel", at = @At("RETURN"))
	private void onCreateLevelEnd(CallbackInfo ci) {
		ResourceLoaderImpl.EXPERIMENTAL_FEATURES_ENABLED.remove();
	}
}
