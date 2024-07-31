/*
 * Copyright 2024 The Quilt Project
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

package org.quiltmc.qsl.tag.mixin.client;

import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.LayeredRegistryManager;
import net.minecraft.registry.ReloadableRegistries;
import net.minecraft.registry.ServerRegistryLayer;
import net.minecraft.resource.ResourceManager;

import org.quiltmc.qsl.tag.impl.client.ClientRegistryStatus;
import org.quiltmc.qsl.tag.impl.client.ClientTagRegistryManager;

@Mixin(ReloadableRegistries.class)
public abstract class ReloadableRegistriesMixin {

	@Inject(method = "reload", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/ReloadableRegistries$TagAwareLookupWrapper;<init>(Lnet/minecraft/registry/DynamicRegistryManager;)V"))
	private static void onLoad(LayeredRegistryManager<ServerRegistryLayer> registryManager, ResourceManager resourceManager, Executor executor, CallbackInfoReturnable<CompletableFuture<LayeredRegistryManager<ServerRegistryLayer>>> cir, @Local DynamicRegistryManager.Frozen registry){
		ClientTagRegistryManager.applyAll(registry, ClientRegistryStatus.LOCAL);
	}
}
