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

package org.quiltmc.qsl.base.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.registry.Registries;

import org.quiltmc.loader.api.entrypoint.EntrypointUtil;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

@Mixin(Registries.class)
public abstract class RegistriesMixin {
	@Inject(method = "bootstrap", at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/Registries;freeze()V"))
	private static void onInitialize(CallbackInfo ci) {
		BootstrapAccessor.invokeSetOutputStreams(); // We need to make this a bit early in case a mod uses System.out to print stuff.

		EntrypointUtil.invoke(ModInitializer.ENTRYPOINT_KEY, ModInitializer.class, ModInitializer::onInitialize);
	}
}
