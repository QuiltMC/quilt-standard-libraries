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
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.world.WorldCreationContext;
import net.minecraft.registry.LayeredRegistryManager;
import net.minecraft.resource.AutoCloseableResourceManager;
import net.minecraft.server.ServerReloadableResources;
import net.minecraft.server.world.FeatureAndDataSettings;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderEventContextsImpl;

@ClientOnly
@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {
	@Dynamic
	@Inject(
			method = "method_41851(Lnet/minecraft/resource/AutoCloseableResourceManager;Lnet/minecraft/server/ServerReloadableResources;Lnet/minecraft/registry/LayeredRegistryManager;Lnet/minecraft/client/gui/screen/world/CreateWorldScreen$WorldCreationSettings;)Lnet/minecraft/client/world/WorldCreationContext;",
			at = @At("HEAD")
	)
	private static void onEndDataPackLoadOnOpen(AutoCloseableResourceManager resourceManager, ServerReloadableResources resources,
			LayeredRegistryManager<?> layeredRegistryManager, @Coerce Object worldCreationSettings, CallbackInfoReturnable<WorldCreationContext> cir) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(new ResourceLoaderEventContextsImpl.ReloadEndContext(
				resourceManager, layeredRegistryManager.getCompositeManager(), Optional.empty()
		));
	}

	// Lambda method in CreateWorldScreen#method_45679, search for a resource manager being closed.
	// Inject before closing the resource manager.
	@Dynamic
	@Inject(
			method = "method_45681(Lnet/minecraft/resource/AutoCloseableResourceManager;Lnet/minecraft/server/ServerReloadableResources;Lnet/minecraft/registry/LayeredRegistryManager;Lnet/minecraft/client/gui/screen/world/CreateWorldScreen$WorldCreationSettings;)Lnet/minecraft/client/world/WorldCreationContext;",
			at = @At("HEAD")
	)
	private static void onCreateDataPackLoadEnd(AutoCloseableResourceManager resourceManager, ServerReloadableResources resources,
			LayeredRegistryManager<?> layeredRegistryManager, @Coerce Object worldCreationSettings, CallbackInfoReturnable<WorldCreationContext> cir) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(new ResourceLoaderEventContextsImpl.ReloadEndContext(
				resourceManager, layeredRegistryManager.getCompositeManager(), Optional.empty()
		));
	}

	// Lambda method in CreateWorldScreen#method_45679, passed CompletableFuture#handle.
	// Take Void and Throwable parameters.
	@Inject(
			slice = @Slice(to = @At(value = "CONSTANT", args = "stringValue=dataPack.validation.failed")),
			method = "method_49629",
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
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(new ResourceLoaderEventContextsImpl.ReloadEndContext(
				null, null, Optional.of(throwable)
		));
	}
}
