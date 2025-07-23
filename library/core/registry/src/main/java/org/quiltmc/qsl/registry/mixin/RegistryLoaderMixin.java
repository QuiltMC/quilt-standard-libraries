/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.registry.mixin;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.HolderLookup;
import net.minecraft.registry.RegistryLoader;
import net.minecraft.resource.ResourceManager;

import org.quiltmc.qsl.registry.api.event.RegistryEvents;
import org.quiltmc.qsl.registry.impl.DynamicRegistryManagerSetupContextImpl;

@Mixin(RegistryLoader.class)
abstract class RegistryLoaderMixin {
	@Shadow
	@Final
	@Mutable
	public static List<RegistryLoader.DecodingData<?>> WORLDGEN_REGISTRIES;

	@Shadow
	@Mutable
	public static List<RegistryLoader.DecodingData<?>> SYNCED_REGISTRIES;

	@Unique
	private static final ThreadLocal<ResourceManager> cachedResourceManager = new ThreadLocal<>();

	static {
		WORLDGEN_REGISTRIES = new ArrayList<>(WORLDGEN_REGISTRIES);
		SYNCED_REGISTRIES = new ArrayList<>(SYNCED_REGISTRIES);
	}

	// TODO is there a better solution for acquiring the used resource manager?
	@Inject(method = "loadFromResource", at = @At("HEAD"))
	private static void cacheResourceManager(
			ResourceManager resourceManager, List<HolderLookup.RegistryLookup<?>> lookups,
			List<RegistryLoader.DecodingData<?>> registryDatas,
			CallbackInfoReturnable<DynamicRegistryManager.Frozen> cir
	) {
		cachedResourceManager.set(resourceManager);
	}

	@Inject(
			method = "load",
			at = @At(
				value = "INVOKE", ordinal = 0, shift = At.Shift.AFTER,
				target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V"
			)
	)
	private static void onBeforeLoad(
			RegistryLoader.LoadingFunction loadingFunction, List<HolderLookup.RegistryLookup<?>> lookups,
			List<RegistryLoader.DecodingData<?>> registryDatas,
			CallbackInfoReturnable<DynamicRegistryManager.Frozen> cir,
			@Local(ordinal = 2) List<RegistryLoader.ContentLoader<?>> list
	) {
		RegistryEvents.DYNAMIC_REGISTRY_SETUP.invoker().onDynamicRegistrySetup(
			new DynamicRegistryManagerSetupContextImpl(
				cachedResourceManager.get(), list.stream().map(RegistryLoader.ContentLoader::registry)
			)
		);
		cachedResourceManager.remove();
	}

	@Inject(method = "load", at = @At(value = "RETURN"))
	private static void onAfterLoad(
			RegistryLoader.LoadingFunction loadingFunction, List<HolderLookup.RegistryLookup<?>> lookups,
			List<RegistryLoader.DecodingData<?>> registryDatas,
			CallbackInfoReturnable<DynamicRegistryManager.Frozen> cir
	) {
		RegistryEvents.DYNAMIC_REGISTRY_LOADED.invoker().onDynamicRegistryLoaded(cir.getReturnValue());
	}
}
