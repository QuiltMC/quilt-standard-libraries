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

package org.quiltmc.qsl.rendering.entity.impl.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

import org.quiltmc.qsl.rendering.entity.api.client.ArmorRenderingRegistry;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class ArmorRenderingRegistryImpl {
	public static final ArmorProviderManager<ArmorRenderingRegistry.TextureProvider>
			TEXTURE_PROVIDERS = new ArmorProviderManager<>();
	public static final ArmorProviderManager<ArmorRenderingRegistry.ModelProvider>
			MODEL_PROVIDERS = new ArmorProviderManager<>();

	private ArmorRenderingRegistryImpl() {
		throw new RuntimeException("ArmorRenderingRegistryImpl only contains static declarations.");
	}
}
