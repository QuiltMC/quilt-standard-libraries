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

import java.util.ArrayList;

import com.mojang.datafixers.util.Pair;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.class_7193;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.resource.AutoCloseableResourceManager;
import net.minecraft.resource.pack.DataPackSettings;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.resource.pack.ResourcePackManager;
import net.minecraft.resource.pack.ResourcePackProfile;
import net.minecraft.server.ServerReloadableResources;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.world.gen.GeneratorOptions;

import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.quiltmc.qsl.resource.loader.impl.ModNioResourcePack;
import org.quiltmc.qsl.resource.loader.impl.ModResourcePackProvider;

@Environment(EnvType.CLIENT)
@Mixin(CreateWorldScreen.class)
public class CreateWorldScreenMixin {
	@ModifyArg(
			method = "createFromExisting",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screen/world/CreateWorldScreen;<init>(Lnet/minecraft/client/gui/screen/Screen;Lnet/minecraft/resource/pack/DataPackSettings;Lnet/minecraft/client/gui/screen/world/MoreOptionsDialog;)V"
			),
			index = 1
	)
	private static DataPackSettings onNew(DataPackSettings settings) {
		var moddedResourcePacks = new ArrayList<ResourcePackProfile>();
		ModResourcePackProvider.SERVER_RESOURCE_PACK_PROVIDER.register(moddedResourcePacks::add);

		var enabled = new ArrayList<>(settings.getEnabled());
		var disabled = new ArrayList<>(settings.getDisabled());

		// This ensure that any built-in registered data packs by mods which needs to be enabled by default are
		// as the data pack screen automatically put any data pack as disabled except the Default data pack.
		for (var profile : moddedResourcePacks) {
			ResourcePack pack = profile.createResourcePack();

			if (pack instanceof ModNioResourcePack modResourcePack && modResourcePack.getActivationType().isEnabledByDefault()) {
				enabled.add(profile.getName());
			} else {
				disabled.add(profile.getName());
			}
		}

		return new DataPackSettings(enabled, disabled);
	}

	@Inject(
			method = "applyDataPacks",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/class_7237;method_42098(Lnet/minecraft/class_7237$class_6906;Lnet/minecraft/class_7237$class_6907;Lnet/minecraft/class_7237$class_7239;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
			)
	)
	private void onDataPackLoadStart(ResourcePackManager dataPackManager, CallbackInfo ci) {
		ResourceLoaderEvents.START_DATA_PACK_RELOAD.invoker().onStartDataPackReload(null, null);
	}

	@Inject(
			method = "method_31130",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/class_7237;method_42098(Lnet/minecraft/class_7237$class_6906;Lnet/minecraft/class_7237$class_6907;Lnet/minecraft/class_7237$class_7239;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
			)
	)
	private static void onDataPackLoadStart(MinecraftClient minecraftClient, Screen screen, CallbackInfo ci) {
		ResourceLoaderEvents.START_DATA_PACK_RELOAD.invoker().onStartDataPackReload(null, null);
	}

	// Lambda method in CreateWorldScreen#applyDataPacks, at class_7237#method_42098.
	// Inject before closing the resource manager.
	@Inject(
			method = "method_41850",
			at = @At("HEAD"),
			remap = false // Very bad
	)
	private static void onDataPackLoadEnd(AutoCloseableResourceManager resourceManager,
	                                      ServerReloadableResources serverReloadableResources,
	                                      DynamicRegistryManager.Frozen frozen, Pair pair,
	                                      CallbackInfoReturnable<class_7193> cir) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, resourceManager, null);
	}

	// Lambda method in CreateWorldScreen#applyDataPacks, at class_7237#method_42098.
	// Inject before closing the resource manager.
	@Inject(
			method = "method_41851",
			at = @At("HEAD"),
			remap = false // Very bad
	)
	private static void onCreateDataPackLoadEnd(AutoCloseableResourceManager resourceManager,
	                                            ServerReloadableResources serverReloadableResources,
	                                            DynamicRegistryManager.Frozen frozen, GeneratorOptions generatorOptions,
	                                            CallbackInfoReturnable<class_7193> cir) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, resourceManager, null);
	}

	// Lambda method in CreateWorldScreen#applyDataPacks, at CompletableFuture#handle.
	// Take Void and Throwable parameters.
	@SuppressWarnings("target")
	@Inject(
			method = "method_37089(Ljava/lang/Void;Ljava/lang/Throwable;)Ljava/lang/Object;",
			at = @At(
					value = "INVOKE",
					target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Throwable;)V",
					remap = false
			)
	)
	private void onFailDataPackLoading(Void unused, Throwable throwable, CallbackInfoReturnable<Object> cir) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, null, throwable);
	}
}
