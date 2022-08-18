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

package org.quiltmc.qsl.rendering.entity.api.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.rendering.entity.impl.client.ArmorRenderingRegistryImpl;

@Environment(EnvType.CLIENT)
public final class ArmorRenderingRegistry {
	private ArmorRenderingRegistry() {
		throw new UnsupportedOperationException("ArmorRenderingRegistry only contains static declarations.");
	}

	public static final Identifier DEFAULT_PHASE = Event.DEFAULT_PHASE;

	public static void registerTextureProvider(@NotNull Identifier phaseIdentifier,
			@NotNull TextureProvider provider, @NotNull ItemConvertible... items) {
		for (var itemC : items) {
			ArmorRenderingRegistryImpl.registerTextureProvider(itemC.asItem(), phaseIdentifier, provider);
		}
	}

	public static void addTextureProviderPhaseOrdering(@NotNull Identifier firstPhase, @NotNull Identifier secondPhase,
			@NotNull ItemConvertible... items) {
		for (var itemC : items) {
			ArmorRenderingRegistryImpl.addTextureProviderPhaseOrdering(itemC.asItem(), firstPhase, secondPhase);
		}
	}

	public static void registerTextureProvider(@NotNull TextureProvider provider, @NotNull ItemConvertible... items) {
		registerTextureProvider(DEFAULT_PHASE, provider, items);
	}

	public static void registerModelProvider(@NotNull Identifier phaseIdentifier,
			@NotNull ModelProvider provider, @NotNull ItemConvertible... items) {
		for (var itemC : items) {
			ArmorRenderingRegistryImpl.registerModelProvider(itemC.asItem(), phaseIdentifier, provider);
		}
	}

	public static void addModelProviderPhaseOrdering(@NotNull Identifier firstPhase, @NotNull Identifier secondPhase,
			@NotNull ItemConvertible... items) {
		for (var itemC : items) {
			ArmorRenderingRegistryImpl.addModelProviderPhaseOrdering(itemC.asItem(), firstPhase, secondPhase);
		}
	}

	public static void registerModelProvider(@NotNull ModelProvider provider, @NotNull ItemConvertible... items) {
		registerModelProvider(DEFAULT_PHASE, provider, items);
	}

	@Environment(EnvType.CLIENT)
	public interface TextureProvider {
		/**
		 * @param useSecondLayer {@code true} to use inner armor (leggings) texture, or {@code false} to use outer armor texture
		 * @param suffix         suffix to append to the end of the texture name
		 */
		@NotNull Identifier getArmorTexture(
				@NotNull Identifier texture,
				@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot,
				boolean useSecondLayer, @Nullable String suffix
		);
	}

	@Environment(EnvType.CLIENT)
	public interface ModelProvider {
		@NotNull BipedEntityModel<LivingEntity> getArmorModel(
				@NotNull BipedEntityModel<LivingEntity> model,
				@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot
		);
	}
}
