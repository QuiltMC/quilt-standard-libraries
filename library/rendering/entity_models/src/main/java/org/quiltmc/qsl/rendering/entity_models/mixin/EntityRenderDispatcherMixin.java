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

package org.quiltmc.qsl.rendering.entity_models.mixin;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.rendering.entity_models.api.animation.AnimationManager;
import org.quiltmc.qsl.rendering.entity_models.api.animation.AnimationManagerContainer;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceType;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin implements AnimationManagerContainer {
	@Unique
	private AnimationManager quilt$animationManager;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void createAnimationManager(MinecraftClient minecraftClient, TextureManager textureManager, ItemRenderer itemRenderer, BlockRenderManager blockRenderManager, TextRenderer textRenderer, GameOptions gameOptions, EntityModelLoader entityModelLoader, CallbackInfo ci) {
		this.quilt$animationManager = new AnimationManager();
		ResourceLoader.get(ResourceType.CLIENT_RESOURCES).registerReloader(this.quilt$animationManager);
	}

	@Override
	@NotNull
	public AnimationManager getAnimationManager() {
		return quilt$animationManager;
	}
}
