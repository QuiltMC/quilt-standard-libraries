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

package org.quiltmc.qsl.rendering.entity.test.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.rendering.entity.api.client.ArmorRenderingRegistry;
import org.quiltmc.qsl.rendering.entity.test.EntityRenderingTestmod;

@Environment(EnvType.CLIENT)
public final class ClientEntityRenderingTestmod implements ClientModInitializer,
		ArmorRenderingRegistry.TextureProvider {
	@Override
	public void onInitializeClient(ModContainer mod) {
		// TEMP DISABLED TO TEST getTexture() METHOD
		/*
		ArmorRenderingRegistry.registerTextureProvider(this,
				EntityRenderingTestmod.QUILT_HELMET, EntityRenderingTestmod.QUILT_CHESTPLATE,
				EntityRenderingTestmod.QUILT_LEGGINGS, EntityRenderingTestmod.QUILT_BOOTS);*/
	}

	@Override
	public @NotNull Identifier getArmorTexture(@NotNull Identifier texture, @NotNull LivingEntity entity,
			@NotNull ItemStack stack, @NotNull EquipmentSlot slot, boolean useSecondLayer, @Nullable String suffix) {
		// simply redirect the texture, so it isn't in Minecraft's texture folder
		return EntityRenderingTestmod.id(texture.getPath());
	}
}
