/*
 * Copyright 2021-2022 QuiltMC
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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Environment(EnvType.CLIENT)
public final class ArmorTextureUtils {
	private ArmorTextureUtils() {
		throw new UnsupportedOperationException("ArmorTextureUtils only contains static declarations.");
	}

	public static @NotNull String getArmorTextureSuffix(boolean useSecondTexture, @Nullable String suffix) {
		return "_layer_" + (useSecondTexture ? 2 : 1) + (suffix == null ? "" : "_" + suffix) + ".png";
	}
}
