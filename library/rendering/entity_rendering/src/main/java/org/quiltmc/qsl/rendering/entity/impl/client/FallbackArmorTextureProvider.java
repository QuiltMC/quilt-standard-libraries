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

package org.quiltmc.qsl.rendering.entity.impl.client;

import java.util.HashMap;
import java.util.Map;

import com.mojang.logging.LogUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import net.minecraft.item.ArmorMaterial;
import net.minecraft.util.Identifier;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class FallbackArmorTextureProvider {
	private FallbackArmorTextureProvider() {
		throw new UnsupportedOperationException("FallbackArmorTextureProvider only contains static declarations.");
	}

	private static final Logger LOGGER = LogUtils.getLogger();
	private static final Map<ArmorMaterial, Identifier> CACHE = new HashMap<>();

	public static @NotNull Identifier getArmorTexture(@NotNull ArmorMaterial material) {
		return CACHE.computeIfAbsent(material, FallbackArmorTextureProvider::createArmorTexture);
	}

	private static @NotNull Identifier createArmorTexture(@NotNull ArmorMaterial material) {
		LOGGER.warn(material.getName() + " (" + material + ") did not implement getTexture()! Using fallback implementation");
		return new Identifier("textures/model/armor/" + material.getName());
	}
}
