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

package org.quiltmc.qsl.tag.mixin.client;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.server.WorldLoader;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.tag.impl.client.ClientRegistryStatus;
import org.quiltmc.qsl.tag.impl.client.ClientTagRegistryManager;

@ClientOnly
@Mixin(WorldLoader.class)
public class WorldLoaderMixin {
	@ModifyArg(
			method = "load",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/ServerReloadableResources;loadResources(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/registry/DynamicRegistryManager$Frozen;Lnet/minecraft/feature_flags/FeatureFlagBitSet;Lnet/minecraft/server/command/CommandManager$RegistrationEnvironment;ILjava/util/concurrent/Executor;Ljava/util/concurrent/Executor;)Ljava/util/concurrent/CompletableFuture;"
			),
			index = 1
	)
	private static DynamicRegistryManager.Frozen onLoad(DynamicRegistryManager.Frozen registry) {
		ClientTagRegistryManager.applyAll(registry, ClientRegistryStatus.LOCAL);
		return registry;
	}

	@Inject(
			method = "load",
			at = @At(
					value = "INVOKE",
					target = "Ljava/util/concurrent/CompletableFuture;failedFuture(Ljava/lang/Throwable;)Ljava/util/concurrent/CompletableFuture;"
			)
	)
	private static void onFailedLoad(WorldLoader.InitConfig initConfig, WorldLoader.LoadContextSupplier<?> loadContextSupplier,
			WorldLoader.ApplierFactory<?, ?> applierFactory, Executor prepareExecutor, Executor applyExecutor,
			CallbackInfoReturnable<CompletableFuture<?>> cir) {
		ClientTagRegistryManager.resetDynamicAll(false);
	}
}
