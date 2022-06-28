package org.quiltmc.qsl.rendering.entity_models.mixin;

import org.quiltmc.qsl.rendering.entity_models.api.AnimationManager;
import org.quiltmc.qsl.rendering.entity_models.api.HasAnimationManager;
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
import net.minecraft.client.render.entity.EntityRendererFactory;
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;

@Mixin(EntityRendererFactory.Context.class)
public class EntityRendererFactoryContextMixin implements HasAnimationManager {
	@Unique
	private AnimationManager animationManager;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void createAnimationManager(EntityRenderDispatcher entityRenderDispatcher, ItemRenderer itemRenderer, BlockRenderManager blockRenderManager, HeldItemRenderer heldItemRenderer, ResourceManager resourceManager, EntityModelLoader entityModelLoader, TextRenderer textRenderer, CallbackInfo ci) {
		this.animationManager = entityRenderDispatcher.getAnimationManager();
	}

	@Override
	public AnimationManager getAnimationManager() {
		return animationManager;
	}
}
