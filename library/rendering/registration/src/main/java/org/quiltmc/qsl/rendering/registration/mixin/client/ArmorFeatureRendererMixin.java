/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.rendering.registration.mixin.client;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

import net.minecraft.client.render.entity.feature.ArmorFeatureRenderer;
import net.minecraft.item.ArmorItem;
import net.minecraft.util.Identifier;

@Mixin(ArmorFeatureRenderer.class)
public class ArmorFeatureRendererMixin {
	@ModifyVariable(method = "getArmorTexture(Lnet/minecraft/item/ArmorItem;ZLjava/lang/String;)Lnet/minecraft/util/Identifier;", at = @At(value = "INVOKE", target = "Ljava/util/Map;computeIfAbsent(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;", remap = false), ordinal = 1)
	private String addTextureNamespace(String path, ArmorItem item, boolean legs, @Nullable String overlay) {
		return item.getMaterial().getTextureNamespace() + Identifier.NAMESPACE_SEPARATOR + path;
	}
}
