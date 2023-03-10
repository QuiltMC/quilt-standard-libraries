/*
 * Copyright 2022-2023 QuiltMC
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

import java.util.List;
import java.util.Map;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.registry.MutableRegistry;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.resource.ResourceManager;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryLoader;

import org.quiltmc.qsl.registry.api.event.RegistryEvents;
import org.quiltmc.qsl.registry.impl.DynamicRegistryManagerSetupContextImpl;

@Mixin(RegistryLoader.class)
public class RegistryLoaderMixin {
	@Inject(
			method = "loadRegistriesIntoManager",
			at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", ordinal = 0, shift = At.Shift.AFTER),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private static void onBeforeLoad(ResourceManager resourceManager, DynamicRegistryManager registryManager, List<RegistryLoader.DecodingData<?>> decodingData,
			CallbackInfoReturnable<DynamicRegistryManager.Frozen> cir,
			Map<?, ?> map,
			List<Pair<MutableRegistry<?>, ?>> registries) {
		RegistryEvents.DYNAMIC_REGISTRY_SETUP.invoker().onDynamicRegistrySetup(
				new DynamicRegistryManagerSetupContextImpl(resourceManager, registries.stream().map(Pair::getFirst))
		);
	}

	@Inject(
			method = "loadRegistriesIntoManager",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V",
					ordinal = 1,
					shift = At.Shift.AFTER
			)
	)
	private static void onAfterLoad(ResourceManager resourceManager, DynamicRegistryManager registryManager, List<RegistryLoader.DecodingData<?>> decodingData,
			CallbackInfoReturnable<DynamicRegistryManager.Frozen> cir) {
		RegistryEvents.DYNAMIC_REGISTRY_LOADED.invoker().onDynamicRegistryLoaded(registryManager);
	}
}
