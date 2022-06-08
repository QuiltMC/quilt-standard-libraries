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

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.Lifecycle;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.SaveLevelScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.world.GeneratorTypes;
import net.minecraft.resource.AutoCloseableResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.DataPackSettings;
import net.minecraft.resource.pack.ResourcePackManager;
import net.minecraft.resource.pack.VanillaDataPackProvider;
import net.minecraft.server.ServerReloadableResources;
import net.minecraft.text.Text;
import net.minecraft.unmapped.C_kjxfcecs;
import net.minecraft.unmapped.C_njsjipmy;
import net.minecraft.util.Util;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryOps;
import net.minecraft.world.gen.GeneratorOptions;

import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.quiltmc.qsl.resource.loader.impl.DataPackLoadingContext;
import org.quiltmc.qsl.resource.loader.impl.ModResourcePackUtil;

@Environment(EnvType.CLIENT)
@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin {
	@Shadow
	@Final
	private static Logger LOGGER;

	@Shadow
	private static C_kjxfcecs.C_kculhjuh method_41849(ResourcePackManager resourcePackManager, DataPackSettings dataPackSettings) {
		throw new IllegalStateException("Mixin injection failed.");
	}

	@Redirect(
			method = "method_31130",
			at = @At(value = "FIELD", target = "Lnet/minecraft/resource/pack/DataPackSettings;SAFE_MODE:Lnet/minecraft/resource/pack/DataPackSettings;")
	)
	private static DataPackSettings replaceDefaultSettings() {
		return ModResourcePackUtil.DEFAULT_SETTINGS;
	}

	@Redirect(
			method = "method_31130",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/unmapped/C_kjxfcecs;method_42098(Lnet/minecraft/unmapped/C_kjxfcecs$C_kculhjuh;Lnet/minecraft/unmapped/C_kjxfcecs$class_6907;Lnet/minecraft/unmapped/C_kjxfcecs$class_7239;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
			)
	)
	private static <D> CompletableFuture<C_njsjipmy> loadDynamicRegistry(C_kjxfcecs.C_kculhjuh initConfig,
			C_kjxfcecs.class_6907<D> arg, C_kjxfcecs.class_7239<D, C_njsjipmy> arg2, Executor executor, Executor executor2) {
		return quilt$applyDefaultDataPacks(C_kjxfcecs.method_42098(initConfig, (resourceManager, dataPackSettings) -> {
			DynamicRegistryManager.Writable registryManager = DynamicRegistryManager.builtInCopy();
			// Force-loads the dynamic registry from data-packs as some mods may define dynamic game objects via data-driven capabilities.
			RegistryOps.createAndLoad(JsonOps.INSTANCE, registryManager, resourceManager);
			DynamicRegistryManager.Frozen frozen = registryManager.freeze();
			GeneratorOptions generatorOptions = GeneratorTypes.create(frozen);
			return Pair.of(generatorOptions, frozen);
		}, (autoCloseableResourceManager, serverReloadableResources, frozen, generatorOptions) -> {
			autoCloseableResourceManager.close();
			return new C_njsjipmy(generatorOptions, Lifecycle.stable(), frozen, serverReloadableResources);
		}, Util.getMainWorkerExecutor(), MinecraftClient.getInstance()));
	}

	@Inject(
			method = "applyDataPacks",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/unmapped/C_kjxfcecs;method_42098(Lnet/minecraft/unmapped/C_kjxfcecs$C_kculhjuh;Lnet/minecraft/unmapped/C_kjxfcecs$class_6907;Lnet/minecraft/unmapped/C_kjxfcecs$class_7239;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
			)
	)
	private void onDataPackLoadStart(ResourcePackManager dataPackManager, CallbackInfo ci) {
		ResourceLoaderEvents.START_DATA_PACK_RELOAD.invoker().onStartDataPackReload(null, null);
	}

	@Inject(
			method = "method_31130",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/unmapped/C_kjxfcecs;method_42098(Lnet/minecraft/unmapped/C_kjxfcecs$C_kculhjuh;Lnet/minecraft/unmapped/C_kjxfcecs$class_6907;Lnet/minecraft/unmapped/C_kjxfcecs$class_7239;Ljava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
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
			CallbackInfoReturnable<C_njsjipmy> cir) {
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
			CallbackInfoReturnable<C_njsjipmy> cir) {
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

	@Unique
	private static CompletableFuture<C_njsjipmy> quilt$applyDefaultDataPacks(CompletableFuture<C_njsjipmy> base) {
		var client = MinecraftClient.getInstance();
		client.send(() -> client.setScreen(new SaveLevelScreen(Text.translatable("dataPack.validation.working"))));

		C_kjxfcecs.C_kculhjuh initConfig = method_41849(new ResourcePackManager(ResourceType.SERVER_DATA, new VanillaDataPackProvider()),
				ModResourcePackUtil.DEFAULT_SETTINGS);
		return C_kjxfcecs.method_42098(
				initConfig,
				(resourceManager, dataPackSettings) -> {
					var dataPackLoadingContext = new DataPackLoadingContext(DynamicRegistryManager.builtInCopy(), resourceManager);
					DataResult<GeneratorOptions> result = dataPackLoadingContext.loadDefaultGeneratorOptions(dataPackLoadingContext.loadRegistries());

					DynamicRegistryManager.Frozen frozenRegistryManager = dataPackLoadingContext.registryManager().freeze();
					Lifecycle lifecycle = result.lifecycle().add(frozenRegistryManager.allElementsLifecycle());
					GeneratorOptions generatorOptions = result.getOrThrow(
							false, Util.addPrefix("Error parsing worldgen settings after loading data-packs: ", LOGGER::error)
					);

					if (frozenRegistryManager.get(Registry.WORLD_PRESET_WORLDGEN).size() == 0) {
						throw new IllegalStateException("Needs at least one world preset to continue");
					} else if (frozenRegistryManager.get(Registry.BIOME_KEY).size() == 0) {
						throw new IllegalStateException("Needs at least one biome to continue");
					} else {
						return Pair.of(Pair.of(generatorOptions, lifecycle), frozenRegistryManager);
					}
				},
				(resourceManager, serverReloadableResources, registryManager, pair) -> {
					resourceManager.close();
					return new C_njsjipmy(pair.getFirst(), pair.getSecond(), registryManager, serverReloadableResources);
				},
				Util.getMainWorkerExecutor(),
				client
		).exceptionallyCompose(error -> {
			LOGGER.warn("Failed to validate default data-pack.", error);
			return base;
		});
	}
}
