/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.tag.mixin;

import com.mojang.serialization.DynamicOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resource.ResourceManager;
import net.minecraft.util.dynamic.RegistryOps;
import net.minecraft.util.registry.DynamicRegistryManager;

import org.quiltmc.qsl.tag.impl.TagRegistryImpl;

/**
 * This mixin loads dynamic registry tags right after data pack entries loaded.
 * Needs a higher priority, so it will be called before biome modifications.
 */
@Mixin(value = RegistryOps.class, priority = 900)
public class RegistryOpsMixin {
	@Inject(method = "method_36574", at = @At("RETURN"))
	private static <T> void afterDynamicRegistryLoaded(DynamicOps<T> dynamicOps, ResourceManager resourceManager,
	                                                   DynamicRegistryManager registryManager, CallbackInfoReturnable<RegistryOps<T>> cir) {
		TagRegistryImpl.loadDynamicRegistryTags(registryManager, resourceManager);
	}
}
