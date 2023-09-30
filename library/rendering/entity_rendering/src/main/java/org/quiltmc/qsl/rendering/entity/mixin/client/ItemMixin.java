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

package org.quiltmc.qsl.rendering.entity.mixin.client;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.item.Item;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.rendering.entity.api.client.ArmorRenderingRegistry;
import org.quiltmc.qsl.rendering.entity.impl.client.ArmorRenderingRegistryImpl;
import org.quiltmc.qsl.rendering.entity.impl.client.ItemArmorRenderingExtensions;

@Mixin(Item.class)
public abstract class ItemMixin implements ItemArmorRenderingExtensions {
	@Unique
	private @Nullable Event<ArmorRenderingRegistry.TextureProvider> quilt$textureProviderEvent;
	@Unique
	private @Nullable Event<ArmorRenderingRegistry.ModelProvider> quilt$modelProviderEvent;
	@Unique
	private @Nullable Event<ArmorRenderingRegistry.RenderLayerProvider> quilt$renderLayerProviderEvent;

	@Override
	public @Nullable Event<ArmorRenderingRegistry.TextureProvider> quilt$getTextureProviderEvent() {
		return this.quilt$textureProviderEvent;
	}

	@Override
	public @Nullable Event<ArmorRenderingRegistry.ModelProvider> quilt$getModelProviderEvent() {
		return this.quilt$modelProviderEvent;
	}

	@Override
	public @Nullable Event<ArmorRenderingRegistry.RenderLayerProvider> quilt$getRenderLayerProviderEvent() {
		return this.quilt$renderLayerProviderEvent;
	}

	@Override
	public synchronized @NotNull Event<ArmorRenderingRegistry.TextureProvider> quilt$getOrCreateTextureProviderEvent() {
		if (this.quilt$textureProviderEvent == null) {
			this.quilt$textureProviderEvent = ArmorRenderingRegistryImpl.createTextureProviderEvent();
		}

		return this.quilt$textureProviderEvent;
	}

	@Override
	public synchronized @NotNull Event<ArmorRenderingRegistry.ModelProvider> quilt$getOrCreateModelProviderEvent() {
		if (this.quilt$modelProviderEvent == null) {
			this.quilt$modelProviderEvent = ArmorRenderingRegistryImpl.createModelProviderEvent();
		}

		return this.quilt$modelProviderEvent;
	}

	@Override
	public synchronized @NotNull Event<ArmorRenderingRegistry.RenderLayerProvider> quilt$getOrCreateRenderLayerProviderEvent() {
		if (this.quilt$renderLayerProviderEvent == null) {
			this.quilt$renderLayerProviderEvent = ArmorRenderingRegistryImpl.createRenderLayerProviderEvent();
		}

		return this.quilt$renderLayerProviderEvent;
	}
}
