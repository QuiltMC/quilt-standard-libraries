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

package org.quiltmc.qsl.resource.loader.mixin.client;

import java.io.File;
import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resource.ClientBuiltinResourcePackProvider;
import net.minecraft.resource.AbstractFileResourcePack;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.ResourcePack;
import net.minecraft.resource.ResourcePackProfile;

import org.quiltmc.qsl.resource.loader.impl.ModResourcePackProvider;
import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

@Mixin(ClientBuiltinResourcePackProvider.class)
public class ClientBuiltinResourcePackProviderMixin {
	@Inject(method = "register", at = @At("RETURN"))
	private void addBuiltinResourcePacks(Consumer<ResourcePackProfile> profileAdder, ResourcePackProfile.Factory factory,
										 CallbackInfo ci) {
		// Register built-in resource packs after vanilla built-in resource packs are registered.
		ModResourcePackProvider.CLIENT_RESOURCE_PACK_PROVIDER.register(profileAdder, factory);
	}

	// Synthetic method register(Consumer;ResourcePackProfile$Factory;)V
	@Dynamic
	@Inject(method = "method_4635", at = @At("RETURN"), cancellable = true, remap = false)
	private void onPackGet(CallbackInfoReturnable<ResourcePack> cir) {
		cir.setReturnValue(ResourceLoaderImpl.buildMinecraftResourcePack((DefaultResourcePack) cir.getReturnValue()));
	}

	// ClientBuiltinResourcePackProvider#method_25454 first lambda.
	@Dynamic
	@Inject(method = "method_25457", at = @At("RETURN"), cancellable = true, remap = false)
	private static void onSupplyZipProgrammerArtPack(File file, CallbackInfoReturnable<ResourcePack> cir) {
		var originalPack = (AbstractFileResourcePack) cir.getReturnValue();
		cir.setReturnValue(ResourceLoaderImpl.buildProgrammerArtResourcePack(originalPack));
	}

	// ClientBuiltinResourcePackProvider#method_25454 second lambda.
	@Dynamic
	@Inject(method = "method_25456", at = @At("RETURN"), cancellable = true, remap = false)
	private static void onSupplyDirProgrammerArtPack(File file, CallbackInfoReturnable<ResourcePack> cir) {
		var originalPack = (AbstractFileResourcePack) cir.getReturnValue();
		cir.setReturnValue(ResourceLoaderImpl.buildProgrammerArtResourcePack(originalPack));
	}
}
