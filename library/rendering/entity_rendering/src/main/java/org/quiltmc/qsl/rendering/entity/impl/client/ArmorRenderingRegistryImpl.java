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
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.rendering.entity.api.client.ArmorRenderingRegistry;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public final class ArmorRenderingRegistryImpl {
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

	public static void registerTextureProvider(@NotNull Item item, @NotNull Identifier phaseIdentifier,
			@NotNull ArmorRenderingRegistry.TextureProvider provider) {
		((ItemExtensions) item).quilt$getOrCreateTextureProviderEvent().register(phaseIdentifier, provider);
	}

	public static void addTextureProviderPhaseOrdering(@NotNull Item item,
			@NotNull Identifier firstPhase, @NotNull Identifier secondPhase) {
		((ItemExtensions) item).quilt$getOrCreateTextureProviderEvent().addPhaseOrdering(firstPhase, secondPhase);
	}

	public static void registerModelProvider(@NotNull Item item, @NotNull Identifier phaseIdentifier,
			@NotNull ArmorRenderingRegistry.ModelProvider provider) {
		((ItemExtensions) item).quilt$getOrCreateModelProviderEvent().register(phaseIdentifier, provider);
	}

	public static void addModelProviderPhaseOrdering(@NotNull Item item,
			@NotNull Identifier firstPhase, @NotNull Identifier secondPhase) {
		((ItemExtensions) item).quilt$getOrCreateModelProviderEvent().addPhaseOrdering(firstPhase, secondPhase);
	}

	public static @NotNull Identifier getArmorTexture(@NotNull Identifier texture,
			@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot,
			boolean useSecondTexture, @Nullable String suffix) {
		var e = ((ItemExtensions) stack.getItem()).quilt$getTextureProviderEvent();
		if (e == null) {
			return texture;
		}
		return e.invoker().getArmorTexture(texture, entity, stack, slot, useSecondTexture, suffix);
	}

	public static @NotNull BipedEntityModel<LivingEntity> getArmorModel(@NotNull BipedEntityModel<LivingEntity> model,
			@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot) {
		var e = ((ItemExtensions) stack.getItem()).quilt$getModelProviderEvent();
		if (e == null) {
			return model;
		}
		return e.invoker().getArmorModel(model, entity, stack, slot);
	}
}
