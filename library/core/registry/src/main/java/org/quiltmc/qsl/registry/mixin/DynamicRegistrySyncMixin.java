/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

import java.util.Map;
import java.util.stream.Stream;

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.DynamicRegistrySync;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;

import org.quiltmc.qsl.registry.impl.dynamic.DynamicRegistryFlagManager;

@Mixin(DynamicRegistrySync.class)
public abstract class DynamicRegistrySyncMixin {
	@SuppressWarnings("unused")	// makes the field mutable for use by the accessor
	@Shadow
	@Final
	@Mutable
	private static Map<RegistryKey<? extends Registry<?>>, ?> SYNCED_CODECS;
	@Unique
	private static boolean filterRegistryEntry(DynamicRegistryManager.RegistryEntry<?> entry) {
		// OPTIONAL
		if (DynamicRegistryFlagManager.isOptional(entry.key().getValue())) {
			return entry.value().size() > 0;
		}

		return true; // If no flags apply, always return true
	}

	@Shadow
	private static Stream<DynamicRegistryManager.RegistryEntry<?>> streamSyncedRegistries(DynamicRegistryManager registryManager) {
		throw new IllegalStateException("Mixin injection failed.");
	}

	/**
	 * This redirect mixin's annotation was taken directly from Fabric API (and adapted for Quilt Mappings), the rest of this file was not.
	 */
	@Dynamic("method_45961: Codec.xmap in buildManagerCodec")
	@Redirect(method = "method_45961",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/registry/DynamicRegistrySync;streamSyncedRegistries(Lnet/minecraft/registry/DynamicRegistryManager;)Ljava/util/stream/Stream;"))
	private static Stream<DynamicRegistryManager.RegistryEntry<?>> filterNonSyncedEntries(DynamicRegistryManager drm) {
		return streamSyncedRegistries(drm).filter(DynamicRegistrySyncMixin::filterRegistryEntry);
	}
}
