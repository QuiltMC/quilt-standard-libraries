package org.quiltmc.qsl.rendering.entity_models.mixin;

import org.quiltmc.qsl.rendering.entity_models.api.animation.AnimationType;
import org.quiltmc.qsl.rendering.entity_models.api.animation.AnimationTypes;
import org.quiltmc.qsl.rendering.entity_models.api.animation.TypedAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.client.render.animation.Animation;

@Mixin(Animation.class)
public class AnimationMixin implements TypedAnimation {
	@Unique
	private AnimationType quilt$animationType = AnimationTypes.QUILT_ANIMATION;

	@Override
	public AnimationType getType() {
		return quilt$animationType;
	}

	@Override
	public void setType(AnimationType type) {
		quilt$animationType = type;
	}
}
