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

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.rendering.entity.impl.client.ArmorRenderingRegistryImpl;

/**
 * Provides methods for modifying the rendering of worn armor on entities.
 */
@ClientOnly
public final class ArmorRenderingRegistry {
	private ArmorRenderingRegistry() {
		throw new UnsupportedOperationException("ArmorRenderingRegistry only contains static declarations.");
	}

	/**
	 * Registers a texture provider for the specified items.
	 *
	 * @param phaseIdentifier the phase identifier
	 * @param provider        the provider
	 * @param items           the items to register for
	 */
	public static void registerTextureProvider(@NotNull Identifier phaseIdentifier,
			@NotNull TextureProvider provider, @NotNull ItemConvertible... items) {
		for (var item : items) {
			ArmorRenderingRegistryImpl.registerTextureProvider(item.asItem(), phaseIdentifier, provider);
		}
	}

	/**
	 * Request that texture providers registered for the specified items for one phase
	 * be executed before providers registered for another phase.
	 *
	 * @param firstPhase  the identifier of the phase that should run before the other. It will be created if it didn't exist yet
	 * @param secondPhase the identifier of the phase that should run after the other. It will be created if it didn't exist yet
	 * @param items       the items to request the phase ordering for
	 */
	public static void addTextureProviderPhaseOrdering(@NotNull Identifier firstPhase, @NotNull Identifier secondPhase,
			@NotNull ItemConvertible... items) {
		for (var item : items) {
			ArmorRenderingRegistryImpl.addTextureProviderPhaseOrdering(item.asItem(), firstPhase, secondPhase);
		}
	}

	/**
	 * Registers a texture provider for the specified items.
	 *
	 * @param provider the provider
	 * @param items    the items to register for
	 */
	public static void registerTextureProvider(@NotNull TextureProvider provider, @NotNull ItemConvertible... items) {
		registerTextureProvider(Event.DEFAULT_PHASE, provider, items);
	}

	/**
	 * Registers a model provider for the specified items.
	 *
	 * @param phaseIdentifier the phase identifier
	 * @param provider        the provider
	 * @param items           the items to register for
	 */
	public static void registerModelProvider(@NotNull Identifier phaseIdentifier,
			@NotNull ModelProvider provider, @NotNull ItemConvertible... items) {
		for (var item : items) {
			ArmorRenderingRegistryImpl.registerModelProvider(item.asItem(), phaseIdentifier, provider);
		}
	}

	/**
	 * Request that model providers registered for the specified items for one phase
	 * be executed before providers registered for another phase.
	 *
	 * @param firstPhase  the identifier of the phase that should run before the other. It will be created if it didn't exist yet
	 * @param secondPhase the identifier of the phase that should run after the other. It will be created if it didn't exist yet
	 * @param items       the items to request the phase ordering for
	 */
	public static void addModelProviderPhaseOrdering(@NotNull Identifier firstPhase, @NotNull Identifier secondPhase,
			@NotNull ItemConvertible... items) {
		for (var item : items) {
			ArmorRenderingRegistryImpl.addModelProviderPhaseOrdering(item.asItem(), firstPhase, secondPhase);
		}
	}

	/**
	 * Registers a model provider for the specified items.
	 *
	 * @param provider the provider
	 * @param items    the items to register for
	 */
	public static void registerModelProvider(@NotNull ModelProvider provider, @NotNull ItemConvertible... items) {
		registerModelProvider(Event.DEFAULT_PHASE, provider, items);
	}

	/**
	 * Registers a static model for the specified items.
	 *
	 * @param model the model
	 * @param items the items to register for
	 */
	public static void registerModel(@NotNull BipedEntityModel<LivingEntity> model, @NotNull ItemConvertible... items) {
		registerModelProvider(Event.DEFAULT_PHASE, (modelx, entity, stack, slot) -> model, items);
	}

	/**
	 * Registers a render layer provider for the specified items.
	 *
	 * @param phaseIdentifier the phase identifier
	 * @param provider        the provider
	 * @param items           the items to register for
	 */
	public static void registerRenderLayerProvider(@NotNull Identifier phaseIdentifier,
			@NotNull RenderLayerProvider provider, @NotNull ItemConvertible... items) {
		for (var item : items) {
			ArmorRenderingRegistryImpl.registerRenderLayerProvider(item.asItem(), phaseIdentifier, provider);
		}
	}

	/**
	 * Request that render layer providers registered for the specified items for one phase
	 * be executed before providers registered for another phase.
	 *
	 * @param firstPhase  the identifier of the phase that should run before the other. It will be created if it didn't exist yet
	 * @param secondPhase the identifier of the phase that should run after the other. It will be created if it didn't exist yet
	 * @param items       the items to request the phase ordering for
	 */
	public static void addRenderLayerProviderPhaseOrdering(@NotNull Identifier firstPhase, @NotNull Identifier secondPhase,
			@NotNull ItemConvertible... items) {
		for (var item : items) {
			ArmorRenderingRegistryImpl.addRenderLayerProviderPhaseOrdering(item.asItem(), firstPhase, secondPhase);
		}
	}

	/**
	 * Registers a render layer provider for the specified items.
	 *
	 * @param provider the provider
	 * @param items    the items to register for
	 */
	public static void registerRenderLayerProvider(@NotNull RenderLayerProvider provider, @NotNull ItemConvertible... items) {
		registerRenderLayerProvider(Event.DEFAULT_PHASE, provider, items);
	}

	/**
	 * Registers a static render layer for the specified items.
	 *
	 * @param layer the render layer
	 * @param items the items to register for
	 */
	public static void registerRenderLayer(@NotNull RenderLayer layer, @NotNull ItemConvertible... items) {
		registerRenderLayerProvider(Event.DEFAULT_PHASE, (layerx, entity, stack, slot, texture) -> layer, items);
	}

	/**
	 * The callback for customizing an armor's texture.
	 */
	@ClientOnly
	public interface TextureProvider {
		/**
		 * Modifies the armor texture.
		 *
		 * @param texture        the <em>current</em> armor texture
		 * @param entity         the entity wearing the armor
		 * @param stack          the item stack representing the worn armor
		 * @param slot           the equipment slot the armor is being worn in
		 * @param useSecondLayer {@code true} to use inner armor (leggings) texture, or {@code false} to use outer armor texture
		 * @param suffix         suffix to append to the end of the texture name
		 * @return the new armor texture, or {@code texture} if the texture should not be changed
		 */
		@NotNull Identifier getArmorTexture(
				@NotNull Identifier texture,
				@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot,
				boolean useSecondLayer, @Nullable String suffix
		);
	}

	/**
	 * The callback for modifying an armor's model.
	 */
	@ClientOnly
	public interface ModelProvider {
		/**
		 * Modifies the armor model.
		 *
		 * @param model  the <em>current</em> armor model
		 * @param entity the entity wearing the armor
		 * @param stack  the item stack representing the worn armor
		 * @param slot   the equipment slot the armor is being worn in
		 * @return the new armor model, or {@code model} if the model should not be changed
		 */
		@NotNull BipedEntityModel<LivingEntity> getArmorModel(
				@NotNull BipedEntityModel<LivingEntity> model,
				@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot
		);
	}

	/**
	 * The callback for modifying an armor's render layer.
	 */
	@ClientOnly
	public interface RenderLayerProvider {
		/**
		 * Modifies the armor render layer.
		 *
		 * @param layer   the <em>current</em> render layer
		 * @param entity  the entity wearing the armor
		 * @param stack   the item stack representing the worn armor
		 * @param slot    the equipment slot the armor is being worn in
		 * @param texture the texture used to render the armor
		 * @return the new armor render layer, or {@code layer} if the layer should not be changed
		 */
		@NotNull RenderLayer getArmorRenderLayer(
				@NotNull RenderLayer layer,
				@NotNull LivingEntity entity, @NotNull ItemStack stack, @NotNull EquipmentSlot slot,
				@NotNull Identifier texture
		);
	}
}
