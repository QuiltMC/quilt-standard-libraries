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
import net.minecraft.client.render.entity.model.EntityModelLoader;
import net.minecraft.client.render.item.ItemRenderer;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.resource.ResourceType;

@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin implements HasAnimationManager {
	@Unique
	private AnimationManager animationManager;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void createAnimationManager(MinecraftClient minecraftClient, TextureManager textureManager, ItemRenderer itemRenderer, BlockRenderManager blockRenderManager, TextRenderer textRenderer, GameOptions gameOptions, EntityModelLoader entityModelLoader, CallbackInfo ci) {
		this.animationManager = new AnimationManager();
		ResourceLoader.get(ResourceType.CLIENT_RESOURCES).registerReloader(this.animationManager);
	}

	@Override
	public AnimationManager getAnimationManager() {
		return animationManager;
	}
}
