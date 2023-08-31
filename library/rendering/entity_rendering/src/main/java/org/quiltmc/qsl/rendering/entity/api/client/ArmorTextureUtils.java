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

package org.quiltmc.qsl.rendering.entity.api.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.loader.api.minecraft.ClientOnly;

/**
 * Provides utility methods for managing armor textures.
 */
@ClientOnly
public final class ArmorTextureUtils {
	private ArmorTextureUtils() {
		throw new UnsupportedOperationException("ArmorTextureUtils only contains static declarations.");
	}

	/**
	 * Creates a vanilla-style armor texture suffix.
	 * <p>
	 * Note that this method does <em>not</em> add the {@code .png} file extension, you need to do this yourself.
	 *
	 * @param useSecondLayer {@code true} to use inner armor (leggings) texture, or {@code false} to use outer armor texture
	 * @param suffix         suffix to append to the end of the texture name
	 * @return the armor texture suffix
	 */
	public static @NotNull String getArmorTextureSuffix(boolean useSecondLayer, @Nullable String suffix) {
		return "_layer_" + (useSecondLayer ? "2" : "1") + (suffix == null ? "" : "_" + suffix);
	}
}
