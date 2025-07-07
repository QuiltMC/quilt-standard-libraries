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

package org.quiltmc.qsl.tag.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.ReloadableRegistries;

import org.quiltmc.qsl.tag.impl.client.ClientRegistryStatus;
import org.quiltmc.qsl.tag.impl.client.ClientTagRegistryManager;

@Mixin(ReloadableRegistries.class)
abstract class ReloadableRegistriesMixin {
	@ModifyExpressionValue(
			method = "reload",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/registry/LayeredRegistryManager;getCompositeUntil(Ljava/lang/Object;)"
					+ "Lnet/minecraft/registry/DynamicRegistryManager$Frozen;"
			)
	)
	private static DynamicRegistryManager.Frozen onLoad(DynamicRegistryManager.Frozen registry) {
		ClientTagRegistryManager.applyAll(registry, ClientRegistryStatus.LOCAL);

		return registry;
	}
}
