/*
 * Copyright 2022 QuiltMC
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

import net.minecraft.util.registry.RegistryOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.RegistryLoader;

import org.quiltmc.qsl.registry.api.event.RegistryEvents;
import org.quiltmc.qsl.registry.impl.DynamicRegistryManagerSetupContextImpl;

@Mixin(RegistryLoader.class)
public class RegistryLoaderMixin {
	@Inject(
			method = "loadRegistriesIntoManager",
			at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", ordinal = 0)
	)
	private static void onBeforeLoad(ResourceManager resourceManager, DynamicRegistryManager registryManager, List<RegistryLoader.DecodingData<?>> decodingData, CallbackInfoReturnable<DynamicRegistryManager.Frozen> cir) {
		RegistryEvents.DYNAMIC_REGISTRY_SETUP.invoker().onDynamicRegistrySetup(
				new DynamicRegistryManagerSetupContextImpl(resourceManager, registryManager)
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
	private static void onAfterLoad(ResourceManager resourceManager, DynamicRegistryManager registryManager, List<RegistryLoader.DecodingData<?>> decodingData, CallbackInfoReturnable<DynamicRegistryManager.Frozen> cir) {
		RegistryEvents.DYNAMIC_REGISTRY_LOADED.invoker().onDynamicRegistryLoaded(registryManager);
	}
}
