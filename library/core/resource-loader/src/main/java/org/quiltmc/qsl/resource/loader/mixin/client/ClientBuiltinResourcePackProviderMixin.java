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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.resource.ClientBuiltinResourcePackProvider;
import net.minecraft.resource.AbstractFileResourcePack;
import net.minecraft.resource.DefaultResourcePack;
import net.minecraft.resource.ResourcePack;

import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

@Mixin(ClientBuiltinResourcePackProvider.class)
public class ClientBuiltinResourcePackProviderMixin {
	// Synthetic method register(Consumer;ResourcePackProfile$Factory;)V
	@Inject(method = "method_4635", at = @At("RETURN"), cancellable = true)
	private void onPackGet(CallbackInfoReturnable<ResourcePack> cir) {
		cir.setReturnValue(ResourceLoaderImpl.buildMinecraftResourcePack((DefaultResourcePack) cir.getReturnValue()));
	}

	// ClientBuiltinResourcePackProvider#method_25454 first lambda.
	@Inject(method = "method_25457", at = @At("RETURN"), cancellable = true)
	private static void onSupplyZipProgrammerArtPack(File file, CallbackInfoReturnable<ResourcePack> cir) {
		var originalPack = (AbstractFileResourcePack) cir.getReturnValue();
		cir.setReturnValue(ResourceLoaderImpl.buildProgrammerArtResourcePack(originalPack));
	}

	// ClientBuiltinResourcePackProvider#method_25454 second lambda.
	@Inject(method = "method_25456", at = @At("RETURN"), cancellable = true)
	private static void onSupplyDirProgrammerArtPack(File file, CallbackInfoReturnable<ResourcePack> cir) {
		var originalPack = (AbstractFileResourcePack) cir.getReturnValue();
		cir.setReturnValue(ResourceLoaderImpl.buildProgrammerArtResourcePack(originalPack));
	}
}
