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

package org.quiltmc.qsl.resource.loader.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.resource.pack.BuiltinResourcePackProvider;
import net.minecraft.resource.pack.ResourcePackProfile;
import net.minecraft.resource.pack.VanillaDataPackProvider;

import org.quiltmc.qsl.resource.loader.impl.ModResourcePackProvider;

@Mixin(BuiltinResourcePackProvider.class)
public class BuiltinResourcePackProviderMixin {
	@SuppressWarnings("ConstantConditions")
	@Inject(method = "registerAdditionalPacks", at = @At("RETURN"))
	private void onRegisterAdditionalPacks(Consumer<ResourcePackProfile> profileAdder, CallbackInfo ci) {
		// Register built-in resource packs after vanilla built-in resource packs are registered.
		if (((Object) this) instanceof VanillaDataPackProvider) {
			ModResourcePackProvider.SERVER_RESOURCE_PACK_PROVIDER.register(profileAdder);
		}
	}
}
