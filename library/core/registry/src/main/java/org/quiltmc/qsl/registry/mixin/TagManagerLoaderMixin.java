/*
 * Copyright 2023 The Quilt Project
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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.tag.TagManagerLoader;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.impl.dynamic.DynamicMetaRegistryImpl;

@Mixin(TagManagerLoader.class)
public class TagManagerLoaderMixin {
	@Inject(method = "getRegistryDirectory", at = @At("HEAD"), cancellable = true)
	private static void quilt$replaceModdedDynamicRegistryTagPath(RegistryKey<? extends Registry<?>> registry, CallbackInfoReturnable<String> cir) {
		Identifier id = registry.getValue();
		if (DynamicMetaRegistryImpl.isModdedRegistryId(id)) {
			cir.setReturnValue("tags/" + id.getNamespace() + "/" + id.getPath());
		}
	}
}
