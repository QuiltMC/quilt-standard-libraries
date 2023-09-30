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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.rendering.entity.api.client.ArmorRenderingRegistry;
import org.quiltmc.qsl.rendering.entity.test.EntityRenderingTestmod;

@ClientOnly
public final class ClientEntityRenderingTestmod implements ClientModInitializer,
		ArmorRenderingRegistry.TextureProvider {
	@Override
	public void onInitializeClient(ModContainer mod) {
		ArmorRenderingRegistry.registerTextureProvider(this, EntityRenderingTestmod.QUILT_LEGGINGS);
	}

	private static final Identifier LEGGINGS_TEXTURE_ID = EntityRenderingTestmod.id("textures/models/armor/overpowered_pants_of_queerness.png");

	@Override
	public @NotNull Identifier getArmorTexture(@NotNull Identifier texture, @NotNull LivingEntity entity,
			@NotNull ItemStack stack, @NotNull EquipmentSlot slot, boolean useSecondLayer, @Nullable String suffix) {
		if (slot == EquipmentSlot.LEGS) {
			// redirect leggings texture, because it has a non-standard name
			return LEGGINGS_TEXTURE_ID;
		}

		return texture;
	}
}
