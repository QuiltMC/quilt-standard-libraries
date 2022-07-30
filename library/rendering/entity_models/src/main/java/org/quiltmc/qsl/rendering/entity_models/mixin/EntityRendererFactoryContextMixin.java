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

import org.quiltmc.qsl.rendering.entity_models.api.AnimationManager;
import org.quiltmc.qsl.rendering.entity_models.api.HasAnimationManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.render.block.BlockRenderManager;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.resource.ResourceManager;

@Mixin(EntityRendererFactory.Context.class)
public class EntityRendererFactoryContextMixin implements HasAnimationManager {
	@Unique
	private AnimationManager quilt$animationManager;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void createAnimationManager(EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer, BlockRenderManager blockRenderManager, HeldItemRenderer heldItemRenderer, ResourceManager resourceManager, EntityModelLoader entityModelLoader, TextRenderer textRenderer, CallbackInfo ci) {
		this.quilt$animationManager = ((HasAnimationManager) entityRenderDispatcher).getAnimationManager();
	}

	@Override
	public AnimationManager getAnimationManager() {
		return quilt$animationManager;
	}
}
