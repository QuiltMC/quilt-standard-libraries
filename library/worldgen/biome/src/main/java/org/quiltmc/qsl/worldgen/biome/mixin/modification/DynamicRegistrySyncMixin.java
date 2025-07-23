/*
 * Copyright 2025 The Quilt Project
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

package org.quiltmc.qsl.worldgen.biome.mixin.modification;

import java.util.List;
import java.util.Set;

import com.mojang.serialization.DynamicOps;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.registry.DynamicRegistrySync;
import net.minecraft.registry.Holder;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryLoader;

import org.quiltmc.qsl.worldgen.biome.impl.modification.BiomeModificationImpl;

@Mixin(DynamicRegistrySync.class)
abstract class DynamicRegistrySyncMixin {
	@ModifyExpressionValue(
			// method_56595 is the Consumer<Holder> lambda passed to registry.streamHolders().forEach(...) in serialize
			method = "method_56595",
			slice = @Slice(from = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/registry/Registry;getRegistrationInfo(Lnet/minecraft/registry/RegistryKey;)"
					+ "Ljava/util/Optional;"
			)),
			at = @At(
				value = "INVOKE", ordinal = 0,
				target = "Ljava/util/Optional;isPresent()Z"
			)
	)
	private static boolean andNotModified(
			boolean fromKnowPack, Registry<?> registry, Set<?> knownPacks, RegistryLoader.DecodingData<?> data,
			DynamicOps<?> ops, List<?> entries, Holder.Reference<?> holder
	) {
		return fromKnowPack && holder.getKey().filter(BiomeModificationImpl::wasModified).isEmpty();
	}
}
