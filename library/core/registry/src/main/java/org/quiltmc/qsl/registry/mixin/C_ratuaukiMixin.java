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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.resource.ResourceManager;
import net.minecraft.unmapped.C_ratuauki;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.RegistryKey;

import org.quiltmc.qsl.registry.api.event.RegistryEvents;

@Mixin(C_ratuauki.class)
public class C_ratuaukiMixin {
	@Inject(
			method = "m_gwtkbndr",
			at = @At(value = "INVOKE", target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V", ordinal = 0),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private static void onBeforeLoad(ResourceManager resourceManager, DynamicRegistryManager baseRegistryManager,
			List<C_ratuauki.C_qpshoosu<?>> entries, CallbackInfoReturnable<DynamicRegistryManager.Frozen> cir,
			Map<RegistryKey<?>, Exception> errors, List<?> list2, DynamicRegistryManager registryManager) {
		RegistryEvents.DYNAMIC_REGISTRY_SETUP.invoker().onDynamicRegistrySetup(resourceManager, registryManager);
	}

	@Inject(
			method = "m_gwtkbndr",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/List;forEach(Ljava/util/function/Consumer;)V",
					ordinal = 1,
					shift = At.Shift.AFTER
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private static void onAfterLoad(ResourceManager resourceManager, DynamicRegistryManager baseRegistryManager,
			List<C_ratuauki.C_qpshoosu<?>> entries, CallbackInfoReturnable<DynamicRegistryManager.Frozen> cir,
			Map<RegistryKey<?>, Exception> errors, List<?> list2, DynamicRegistryManager registryManager) {
		RegistryEvents.DYNAMIC_REGISTRY_LOADED.invoker().onDynamicRegistryLoaded(registryManager);
	}
}
