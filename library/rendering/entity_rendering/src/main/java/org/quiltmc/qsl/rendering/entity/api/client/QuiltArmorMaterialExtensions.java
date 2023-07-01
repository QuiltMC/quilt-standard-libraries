/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.rendering.entity.api.client;

import org.jetbrains.annotations.NotNull;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.util.InjectedInterface;
import org.quiltmc.qsl.rendering.entity.impl.client.FallbackArmorTextureProvider;

/**
 * Extensions of {@link ArmorMaterial}, allowing for customizing the armor texture without having to use the
 * {@link ArmorRenderingRegistry.TextureProvider} API.
 */
@InjectedInterface(ArmorMaterial.class)
public interface QuiltArmorMaterialExtensions {
	/**
	 * Gets the base texture identifier to use for this armor material.
	 * <p>
	 * This will automatically have a {@code "_layer_1/2(_overlay).png"} suffix added to it, depending on the parameters
	 * the vanilla game decides on.
	 *
	 * @see ArmorTextureUtils#getArmorTextureSuffix(boolean, String)
	 * @return the base texture identifier
	 */
	@ClientOnly
	default @NotNull Identifier getTexture() {
		return FallbackArmorTextureProvider.getArmorTexture((ArmorMaterial) this);
	}
}
