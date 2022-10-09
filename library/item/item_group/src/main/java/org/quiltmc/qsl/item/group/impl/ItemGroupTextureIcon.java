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

package org.quiltmc.qsl.item.group.impl;

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.item.group.api.ItemGroupIcon;
import org.quiltmc.qsl.resource.loader.api.client.ClientResourceLoaderEvents;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

@ApiStatus.Internal
public final class ItemGroupTextureIcon implements ItemGroupIcon, ClientResourceLoaderEvents.EndResourcePackReload {
	// NB: These have to be suppliers since these objects are created before minecraft has a instance?
	private final Supplier<TextureManager> textureManagerSupplier = () -> MinecraftClient.getInstance().getTextureManager();
	private final Supplier<ResourceManager> resourceManagerSupplier = () -> MinecraftClient.getInstance().getResourceManager();
	private final Identifier textureId;
	private int textureGlId = -1;

	public ItemGroupTextureIcon(Identifier textureId) {
		this.textureId = textureId;
		ClientResourceLoaderEvents.END_RESOURCE_PACK_RELOAD.register(this);
	}

	@Override
	public void render(MatrixStack matrices, int x, int y) {
		if (textureGlId == -1) {
			if (resourceManagerSupplier.get().getResource(textureId).isPresent()) {
				textureGlId = textureManagerSupplier.get().getTexture(textureId).getGlId();
			} else {
				textureGlId = MissingSprite.getMissingSpriteTexture().getGlId();
			}
		}

		RenderSystem.setShaderTexture(0, textureGlId);
		DrawableHelper.drawTexture(matrices, x, y, 0f, 0f, 16, 16, 16, 16);
	}

	@Override
	public void onEndResourcePackReload(MinecraftClient client, ResourceManager resourceManager, boolean first, @Nullable Throwable error) {
		// NB: We have to revalue the gl id because the icon texture might not exist now.
		textureGlId = -1;
	}
}
