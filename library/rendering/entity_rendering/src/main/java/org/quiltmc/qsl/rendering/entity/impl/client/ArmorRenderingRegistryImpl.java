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
import org.slf4j.Logger;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.state.BipedRenderState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.EquipmentAsset;
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
				listeners -> (texture, entity, stack, slot, useSecondTexture) -> {
					for (ArmorRenderingRegistry.TextureProvider listener : listeners) {
						texture = listener.getArmorTexture(texture, entity, stack, slot, useSecondTexture);
					}

					return texture;
				});
	}

	@Contract("-> new")
	public static @NotNull Event<ArmorRenderingRegistry.ModelProvider> createModelProviderEvent() {
		return Event.create(ArmorRenderingRegistry.ModelProvider.class,
				listeners -> (model, entity, stack, slot) -> {
					for (ArmorRenderingRegistry.ModelProvider listener : listeners) {
						model = listener.getArmorModel(model, entity, stack, slot);
					}

					return model;
				});
	}

	@Contract("-> new")
	public static @NotNull Event<ArmorRenderingRegistry.RenderLayerProvider> createRenderLayerProviderEvent() {
		return Event.create(ArmorRenderingRegistry.RenderLayerProvider.class,
				listeners -> (layer, state, stack, slot, texture) -> {
					for (ArmorRenderingRegistry.RenderLayerProvider listener : listeners) {
						layer = listener.getArmorRenderLayer(layer, state, stack, slot, texture);
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

	public static @NotNull RegistryKey<EquipmentAsset> getArmorAsset(
			@NotNull RegistryKey<EquipmentAsset> asset,
			@NotNull BipedRenderState state, @NotNull ItemStack stack, @NotNull EquipmentSlot slot,
			boolean useSecondTexture
	) {
		var e = ((ItemArmorRenderingExtensions) stack.getItem()).quilt$getTextureProviderEvent();
		if (e == null) {
			return asset;
		}

		return e.invoker().getArmorTexture(asset, state, stack, slot, useSecondTexture);
	}

	public static @NotNull BipedEntityModel<BipedRenderState> getArmorModel(
			@NotNull BipedEntityModel<BipedRenderState> model,
			@NotNull BipedRenderState state,
			@NotNull ItemStack stack,
			@NotNull EquipmentSlot slot
	) {
		var e = ((ItemArmorRenderingExtensions) stack.getItem()).quilt$getModelProviderEvent();
		if (e == null) {
			return model;
		}

		return e.invoker().getArmorModel(model, state, stack, slot);
	}

	public static @NotNull RenderLayer getArmorRenderLayer(
			@NotNull RenderLayer layer, @NotNull BipedRenderState state, @NotNull ItemStack stack,
			@NotNull EquipmentSlot slot, @NotNull RegistryKey<EquipmentAsset> armorAsset
	) {
		Event<ArmorRenderingRegistry.RenderLayerProvider> event =
				((ItemArmorRenderingExtensions) stack.getItem()).quilt$getRenderLayerProviderEvent();
		if (event == null) {
			return layer;
		}

		return event.invoker().getArmorRenderLayer(layer, state, stack, slot, armorAsset);
	}
}
