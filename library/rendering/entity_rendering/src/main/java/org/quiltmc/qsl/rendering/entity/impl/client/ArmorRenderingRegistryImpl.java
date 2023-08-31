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

import com.mojang.logging.LogUtils;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.rendering.entity.api.client.ArmorRenderingRegistry;

@ApiStatus.Internal
@ClientOnly
public final class ArmorRenderingRegistryImpl {
	public static final Logger LOGGER = LogUtils.getLogger();

	private ArmorRenderingRegistryImpl() {
		throw new UnsupportedOperationException("ArmorRenderingRegistryImpl only contains static declarations.");
	}

	@Contract("-> new")
	public static @NotNull Event<ArmorRenderingRegistry.TextureProvider> createTextureProviderEvent() {
		return Event.create(ArmorRenderingRegistry.TextureProvider.class,
				listeners -> (texture, entity, stack, slot, useSecondTexture, suffix) -> {
					for (var listener : listeners) {
						texture = listener.getArmorTexture(texture, entity, stack, slot, useSecondTexture, suffix);
					}

					return texture;
				});
	}

	@Contract("-> new")
	public static @NotNull Event<ArmorRenderingRegistry.ModelProvider> createModelProviderEvent() {
		return Event.create(ArmorRenderingRegistry.ModelProvider.class,
				listeners -> (model, entity, stack, slot) -> {
					for (var listener : listeners) {
						model = listener.getArmorModel(model, entity, stack, slot);
					}

					return model;
				});
	}

	@Contract("-> new")
	public static @NotNull Event<ArmorRenderingRegistry.RenderLayerProvider> createRenderLayerProviderEvent() {
		return Event.create(ArmorRenderingRegistry.RenderLayerProvider.class,
				listeners -> (layer, entity, stack, slot, texture) -> {
					for (var listener : listeners) {
						layer = listener.getArmorRenderLayer(layer, entity, stack, slot, texture);
					}

					return layer;
				});
	}

	public static void registerTextureProvider(@NotNull Item item, @NotNull Identifier phaseIdentifier,
			@NotNull ArmorRenderingRegistry.TextureProvider provider) {
		((ItemArmorRenderingExtensions) item).quilt$getOrCreateTextureProviderEvent().register(phaseIdentifier, provider);
	}

	public static void addTextureProviderPhaseOrdering(@NotNull Item item,
			@NotNull Identifier firstPhase, @NotNull Identifier secondPhase) {
		((ItemArmorRenderingExtensions) item).quilt$getOrCreateTextureProviderEvent().addPhaseOrdering(firstPhase, secondPhase);
	}

	public static void registerModelProvider(@NotNull Item item, @NotNull Identifier phaseIdentifier,
			@NotNull ArmorRenderingRegistry.ModelProvider provider) {
		((ItemArmorRenderingExtensions) item).quilt$getOrCreateModelProviderEvent().register(phaseIdentifier, provider);
	}

	public static void addModelProviderPhaseOrdering(@NotNull Item item,
			@NotNull Identifier firstPhase, @NotNull Identifier secondPhase) {
		((ItemArmorRenderingExtensions) item).quilt$getOrCreateModelProviderEvent().addPhaseOrdering(firstPhase, secondPhase);
	}

	public static void registerRenderLayerProvider(@NotNull Item item, @NotNull Identifier phaseIdentifier,
			@NotNull ArmorRenderingRegistry.RenderLayerProvider provider) {
		((ItemArmorRenderingExtensions) item).quilt$getOrCreateRenderLayerProviderEvent().register(phaseIdentifier, provider);
	}

	public static void addRenderLayerProviderPhaseOrdering(@NotNull Item item,
			@NotNull Identifier firstPhase, @NotNull Identifier secondPhase) {
		((ItemArmorRenderingExtensions) item).quilt$getOrCreateRenderLayerProviderEvent().addPhaseOrdering(firstPhase, secondPhase);
	}

	public static @NotNull Identifier getArmorTexture(@NotNull Identifier texture,
			@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot,
			boolean useSecondTexture, @Nullable String suffix) {
		var e = ((ItemArmorRenderingExtensions) stack.getItem()).quilt$getTextureProviderEvent();
		if (e == null) {
			return texture;
		}

		return e.invoker().getArmorTexture(texture, entity, stack, slot, useSecondTexture, suffix);
	}

	public static @NotNull BipedEntityModel<LivingEntity> getArmorModel(@NotNull BipedEntityModel<LivingEntity> model,
			@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot) {
		var e = ((ItemArmorRenderingExtensions) stack.getItem()).quilt$getModelProviderEvent();
		if (e == null) {
			return model;
		}

		return e.invoker().getArmorModel(model, entity, stack, slot);
	}

	public static @NotNull RenderLayer getArmorRenderLayer(@NotNull RenderLayer layer,
			@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot,
			@NotNull Identifier texture) {
		var e = ((ItemArmorRenderingExtensions) stack.getItem()).quilt$getRenderLayerProviderEvent();
		if (e == null) {
			return layer;
		}

		return e.invoker().getArmorRenderLayer(layer, entity, stack, slot, texture);
	}
}
