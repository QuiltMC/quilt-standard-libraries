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

package org.quiltmc.qsl.registry.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.impl.dynamic.DynamicMetaRegistryImpl;

@Mixin(RegistryKeys.class)
public class RegistryKeysMixin {
	@WrapOperation(
			method = {"getDirectory", "getTagDirectory"},
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/util/Identifier;getPath()Ljava/lang/String;"
			)
	)
	private static String replaceDynamicRegistryPath(Identifier id, Operation<String> original) {
		if (DynamicMetaRegistryImpl.isModdedRegistryId(id)) {
			return id.getNamespace() + "/" + original.call(id);
		}

		return original.call(id);
	}
}
