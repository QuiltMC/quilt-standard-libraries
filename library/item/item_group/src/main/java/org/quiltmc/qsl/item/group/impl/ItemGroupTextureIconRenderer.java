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

package org.quiltmc.qsl.item.group.impl;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.texture.MissingSprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemGroup;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.item.group.api.client.ItemGroupIconRenderer;
import org.quiltmc.qsl.resource.loader.api.client.ClientResourceLoaderEvents;

@ApiStatus.Internal
public final class ItemGroupTextureIconRenderer<IG extends ItemGroup> implements ItemGroupIconRenderer<IG>, ClientResourceLoaderEvents.EndResourcePackReload {
	private final Identifier textureId;
	private int textureGlId = -1;

	public ItemGroupTextureIconRenderer(Identifier textureId) {
		this.textureId = textureId;
		ClientResourceLoaderEvents.END_RESOURCE_PACK_RELOAD.register(this);
	}

	@Override
	public void render(IG itemGroup, MatrixStack matrices, int x, int y, float tickDelta) {
		if (this.textureGlId == -1) {
			if (MinecraftClient.getInstance().getResourceManager().getResource(this.textureId).isPresent()) {
				this.textureGlId = MinecraftClient.getInstance().getTextureManager().getTexture(this.textureId).getGlId();
			} else {
				this.textureGlId = MissingSprite.getMissingSpriteTexture().getGlId();
			}
		}

		RenderSystem.setShaderTexture(0, this.textureGlId);
		DrawableHelper.drawTexture(matrices, x, y, 0f, 0f, 16, 16, 16, 16);
	}

	@Override
	public void onEndResourcePackReload(MinecraftClient client, ResourceManager resourceManager, boolean first, @Nullable Throwable error) {
		// NB: We have to revalue the gl id because the icon texture might not exist now.
		this.textureGlId = -1;
	}
}
