/*
 * Copyright 2021-2023 QuiltMC
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

package org.quiltmc.qsl.resource.loader.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.resource.ResourceType;
import net.minecraft.resource.pack.ResourcePack;
import net.minecraft.resource.pack.VanillaDataPackProvider;

import org.quiltmc.qsl.resource.loader.impl.ResourceLoaderImpl;

@Mixin(VanillaDataPackProvider.class)
public class VanillaDataPackProviderMixin {
	// Synthetic method createBuiltinResourcePackProfile(ResourcePack)ResourcePackProfile -> lambda in ResourcePackProfile.of
	// Using an injector to wrap the previous return value.
	@Inject(
			method = "m_kvlgyntq(Lnet/minecraft/resource/pack/ResourcePack;Ljava/lang/String;)Lnet/minecraft/resource/pack/ResourcePack;",
			at = @At("RETURN"),
			cancellable = true
	)
	private static void onPackGet(ResourcePack pack, String name, CallbackInfoReturnable<ResourcePack> cir) {
		cir.setReturnValue(ResourceLoaderImpl.buildMinecraftResourcePack(ResourceType.SERVER_DATA, cir.getReturnValue()));
	}
}
