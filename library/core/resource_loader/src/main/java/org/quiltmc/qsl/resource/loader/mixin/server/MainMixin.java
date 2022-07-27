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

package org.quiltmc.qsl.resource.loader.mixin.server;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.Lifecycle;
import joptsimple.OptionSet;
import joptsimple.OptionSpec;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.nbt.NbtElement;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.pack.DataPackSettings;
import net.minecraft.server.Main;
import net.minecraft.server.WorldStem;
import net.minecraft.server.dedicated.ServerPropertiesLoader;
import net.minecraft.util.Util;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.gen.GeneratorOptions;
import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;
import net.minecraft.world.level.storage.LevelStorage;

import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;
import org.quiltmc.qsl.resource.loader.impl.DataPackLoadingContext;
import org.quiltmc.qsl.resource.loader.impl.ModResourcePackUtil;

@Mixin(Main.class)
public class MainMixin {
	@Shadow
	@Final
	private static Logger LOGGER;

	@ModifyArg(
			method = "main",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/Objects;requireNonNullElse(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;"
			),
			index = 1,
			remap = false
	)
	private static Object replaceDefaultDataPackSettings(Object base) {
		return ModResourcePackUtil.DEFAULT_SETTINGS;
	}

	@Inject(
			method = "main",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/util/Util;method_43499(Ljava/util/function/Function;)Ljava/util/concurrent/CompletableFuture;",
					remap = true
			),
			remap = false
	)
	private static void onStartReloadResources(String[] strings, CallbackInfo ci) {
		ResourceLoaderEvents.START_DATA_PACK_RELOAD.invoker().onStartDataPackReload(null, null); // First reload
	}

	@ModifyVariable(method = "main", at = @At(value = "STORE"), remap = false)
	private static WorldStem onSuccessfulReloadResources(WorldStem resources) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, resources.resourceManager(), null);
		return resources; // noop
	}

	@ModifyArg(
			method = "main",
			at = @At(
					value = "INVOKE",
					target = "Lorg/slf4j/Logger;warn(Ljava/lang/String;Ljava/lang/Throwable;)V"
			),
			index = 1,
			remap = false
	)
	private static Throwable onFailedReloadResources(Throwable exception) {
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.invoker().onEndDataPackReload(null, null, exception);
		return exception; // noop
	}

	@Inject(
			method = {"m_cbzunkqe", "method_43613"},
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/world/level/LevelProperties;<init>(Lnet/minecraft/world/level/LevelInfo;Lnet/minecraft/world/gen/GeneratorOptions;Lcom/mojang/serialization/Lifecycle;)V",
					remap = true
			),
			remap = false,
			require = 1,
			cancellable = true,
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private static void onLoadDataPacks(LevelStorage.Session session, OptionSet optionSet, OptionSpec optionSpec,
			ServerPropertiesLoader serverPropertiesLoader, OptionSpec optionSpec2,
			ResourceManager resourceManager, DataPackSettings dataPackSettings,
			CallbackInfoReturnable<Pair<SaveProperties, DynamicRegistryManager.Frozen>> cir,
			DynamicRegistryManager.Writable writable,
			DynamicOps<NbtElement> nbtRegistryOps,
			SaveProperties saveProperties,
			LevelInfo levelInfo,
			GeneratorOptions existingGeneratorOptions) {
		var dataPackLoadingContext = new DataPackLoadingContext(writable, resourceManager);
		DataResult<GeneratorOptions> result = dataPackLoadingContext.loadGeneratorOptions(existingGeneratorOptions, dataPackLoadingContext.loadRegistries());

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
			var levelProperties = new LevelProperties(levelInfo, generatorOptions, lifecycle);
			cir.setReturnValue(Pair.of(levelProperties, frozenRegistryManager));
		}
	}
}
