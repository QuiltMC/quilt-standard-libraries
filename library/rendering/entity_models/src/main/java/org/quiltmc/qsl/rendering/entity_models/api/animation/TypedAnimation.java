package org.quiltmc.qsl.rendering.entity_models.api.animation;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

import net.minecraft.client.render.animation.Animation;

/**
 * An injected interface on {@link net.minecraft.client.render.animation.Animation} to specify its type.
 * Defaults to {@link org.quiltmc.qsl.rendering.entity_models.api.animation.AnimationTypes#QUILT_ANIMATION}.
 */
@InjectedInterface(Animation.class)
public interface TypedAnimation {
	AnimationType getType();
	void setType(AnimationType type);
}
