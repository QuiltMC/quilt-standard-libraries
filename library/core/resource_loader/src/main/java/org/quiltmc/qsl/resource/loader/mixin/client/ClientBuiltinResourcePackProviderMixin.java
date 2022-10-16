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

import java.io.File;
import java.util.function.Consumer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resource.ClientBuiltinResourcePackProvider;
import net.minecraft.resource.pack.AbstractFileResourcePack;
import net.minecraft.resource.pack.DefaultResourcePack;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.resource.pack.ResourcePackProfile;

import org.quiltmc.qsl.resource.loader.impl.ModResourcePackProvider;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

@Environment(EnvType.CLIENT)
@Mixin(ClientBuiltinResourcePackProvider.class)
public class ClientBuiltinResourcePackProviderMixin {
	@Inject(method = "register", at = @At("RETURN"))
	private void addBuiltinResourcePacks(Consumer<ResourcePackProfile> profileAdder, ResourcePackProfile.Factory factory,
			CallbackInfo ci) {
		// Register built-in resource packs after vanilla built-in resource packs are registered.
		ModResourcePackProvider.CLIENT_RESOURCE_PACK_PROVIDER.register(profileAdder, factory);
	}

	// Synthetic method register(Consumer;ResourcePackProfile$Factory;)V
	// Using an injector to wrap the previous return value.
	@Dynamic
	@Inject(method = "m_mclyvngx()Lnet/minecraft/resource/pack/ResourcePack;", at = @At("RETURN"), cancellable = true)
	private void onPackGet(CallbackInfoReturnable<ResourcePack> cir) {
		cir.setReturnValue(ResourceLoaderImpl.buildMinecraftResourcePack((DefaultResourcePack) cir.getReturnValue()));
	}

	// ClientBuiltinResourcePackProvider#getProgrammerArtResourcePackProfile first lambda.
	// Using an injector to wrap the previous return value.
	@Dynamic
	@Inject(method = "m_yhfpoaqv(Ljava/io/File;)Lnet/minecraft/resource/pack/ResourcePack;", at = @At("RETURN"), cancellable = true)
	private static void onSupplyZipProgrammerArtPack(File file, CallbackInfoReturnable<ResourcePack> cir) {
		var originalPack = (AbstractFileResourcePack) cir.getReturnValue();
		cir.setReturnValue(ResourceLoaderImpl.buildProgrammerArtResourcePack(originalPack));
	}

	// ClientBuiltinResourcePackProvider#getProgrammerArtResourcePackProfile second lambda.
	// Using an injector to wrap the previous return value.
	@Dynamic
	@Inject(method = "m_hiwhulzd(Ljava/io/File;)Lnet/minecraft/resource/pack/ResourcePack;", at = @At("RETURN"), cancellable = true)
	private static void onSupplyDirProgrammerArtPack(File file, CallbackInfoReturnable<ResourcePack> cir) {
		var originalPack = (AbstractFileResourcePack) cir.getReturnValue();
		cir.setReturnValue(ResourceLoaderImpl.buildProgrammerArtResourcePack(originalPack));
	}
}
