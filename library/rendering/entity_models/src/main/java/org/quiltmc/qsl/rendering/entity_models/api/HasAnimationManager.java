package org.quiltmc.qsl.rendering.entity_models.api;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRendererFactory;

@InjectedInterface(value = {
		EntityRenderDispatcher.class,
		EntityRendererFactory.Context.class
})
public interface HasAnimationManager {
	AnimationManager getAnimationManager();
}
