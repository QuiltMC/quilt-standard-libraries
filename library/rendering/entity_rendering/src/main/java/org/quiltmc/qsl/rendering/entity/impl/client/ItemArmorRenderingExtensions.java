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

package org.quiltmc.qsl.rendering.entity.impl.client;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.rendering.entity.api.client.ArmorRenderingRegistry;

@ApiStatus.Internal
@ClientOnly
public interface ItemArmorRenderingExtensions {
	@Nullable Event<ArmorRenderingRegistry.TextureProvider> quilt$getTextureProviderEvent();
	@Nullable Event<ArmorRenderingRegistry.ModelProvider> quilt$getModelProviderEvent();
	@Nullable Event<ArmorRenderingRegistry.RenderLayerProvider> quilt$getRenderLayerProviderEvent();

	@NotNull Event<ArmorRenderingRegistry.TextureProvider> quilt$getOrCreateTextureProviderEvent();
	@NotNull Event<ArmorRenderingRegistry.ModelProvider> quilt$getOrCreateModelProviderEvent();
	@NotNull Event<ArmorRenderingRegistry.RenderLayerProvider> quilt$getOrCreateRenderLayerProviderEvent();
}
