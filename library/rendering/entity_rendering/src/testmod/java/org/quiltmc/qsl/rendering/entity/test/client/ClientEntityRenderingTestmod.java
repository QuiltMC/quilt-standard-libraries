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

package org.quiltmc.qsl.rendering.entity.test.client;

import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModelLayers;
import net.minecraft.client.render.entity.model.EntityModelPartNames;
import net.minecraft.client.render.entity.state.BipedRenderState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.EquipmentAsset;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.rendering.entity.api.client.ArmorRenderingRegistry;
import org.quiltmc.qsl.rendering.entity.test.EntityRenderingTestmod;

@ClientOnly
public final class ClientEntityRenderingTestmod implements
		ClientModInitializer,
		ArmorRenderingRegistry.TextureProvider,
		ArmorRenderingRegistry.ModelProvider,
		ArmorRenderingRegistry.RenderLayerProvider {
	@Override
	public void onInitializeClient(ModContainer mod) {
		ArmorRenderingRegistry.registerTextureProvider(this, EntityRenderingTestmod.QUILT_LEGGINGS);
		ArmorRenderingRegistry.registerModelProvider(this, EntityRenderingTestmod.QUILT_HELMET);
		ArmorRenderingRegistry.registerRenderLayerProvider(this, EntityRenderingTestmod.QUILT_HELMET);
	}

	private static final RegistryKey<EquipmentAsset> LEGGINGS_KEY = EntityRenderingTestmod
			.createAssetKey("quilt_leggings");

	private static @NotNull BipedEntityModel<BipedRenderState> getWitchHeadModel() {
		ModelPart witchHeadPart = MinecraftClient.getInstance().getEntityModelLoader()
				.getModelPart(EntityModelLayers.WITCH)
				.getChild(EntityModelPartNames.HEAD);

		return new BipedEntityModel<>(
			new ModelPart(
				List.of(),
				Map.of(
					// HEAD part must have a HAT child.
					// Only the HEAD part of the model is actually used since this is for a helmet,
					// so just pass the head part to all of them.
					EntityModelPartNames.HEAD, witchHeadPart,
					EntityModelPartNames.BODY, witchHeadPart,
					EntityModelPartNames.RIGHT_ARM, witchHeadPart,
					EntityModelPartNames.LEFT_ARM, witchHeadPart,
					EntityModelPartNames.RIGHT_LEG, witchHeadPart,
					EntityModelPartNames.LEFT_LEG, witchHeadPart
				)
			)
		);
	}

	@Override
	public @NotNull RegistryKey<EquipmentAsset> getArmorTexture(
			@NotNull RegistryKey<EquipmentAsset> texture, @NotNull BipedRenderState state,
			@NotNull ItemStack stack, @NotNull EquipmentSlot slot, boolean useSecondLayer
	) {
		if (slot == EquipmentSlot.LEGS) {
			// redirect leggings texture, because it has a non-standard name
			return LEGGINGS_KEY;
		} else {
			return texture;
		}
	}

	@Override
	public @NotNull BipedEntityModel<BipedRenderState> getArmorModel(
			@NotNull BipedEntityModel<BipedRenderState> model, @NotNull BipedRenderState state,
			@NotNull ItemStack stack, @NotNull EquipmentSlot slot
	) {
		if (slot == EquipmentSlot.HEAD) {
			return getWitchHeadModel();
		} else {
			return model;
		}
	}

	@Override
	public @NotNull RenderLayer getArmorRenderLayer(
			@NotNull RenderLayer layer, @NotNull BipedRenderState state, @NotNull ItemStack stack,
			@NotNull EquipmentSlot slot, @NotNull RegistryKey<EquipmentAsset> armorAsset
	) {
		// this render layer is required since we use the witch head model for the quilt_helmet
		return RenderLayer.getEntityCutoutNoCull(Identifier.ofDefault("textures/entity/witch.png"));
	}
}
