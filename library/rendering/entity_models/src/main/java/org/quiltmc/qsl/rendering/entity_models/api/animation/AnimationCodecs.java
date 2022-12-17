package org.quiltmc.qsl.rendering.entity_models.api.animation;

import java.util.Arrays;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.quiltmc.qsl.rendering.entity_models.api.Codecs;

import net.minecraft.client.render.animation.Animation;
import net.minecraft.client.render.animation.AnimationKeyframe;
import net.minecraft.client.render.animation.PartAnimation;

/**
 * Codecs for animation loading
 */
public final class AnimationCodecs {
	public static final Codec<AnimationKeyframe> KEYFRAME = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.floatRange(0, Float.MAX_VALUE).fieldOf("timestamp").forGetter(AnimationKeyframe::timestamp),
					Codecs.VECTOR_3F_CODEC.fieldOf("transformation").forGetter(AnimationKeyframe::transformation),
					AnimationInterpolations.CODEC.fieldOf("interpolator").forGetter(AnimationKeyframe::interpolator)
			).apply(instance, AnimationKeyframe::new)
	);

	public static final Codec<PartAnimation> PART_ANIMATION = RecordCodecBuilder.create(instance ->
			instance.group(
					AnimationTransformations.CODEC.fieldOf("transformation").forGetter(PartAnimation::transformation),
					Codec.list(KEYFRAME).xmap(list -> list.toArray(AnimationKeyframe[]::new), Arrays::asList).fieldOf("keyframes").forGetter(PartAnimation::keyframes)
			).apply(instance, PartAnimation::new)
	);

	public static final Codec<Animation> ANIMATION = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.floatRange(0, Float.MAX_VALUE).fieldOf("length").forGetter(Animation::length),
					Codec.BOOL.fieldOf("looping").forGetter(Animation::looping),
					Codec.unboundedMap(Codec.STRING, Codec.list(PART_ANIMATION)).fieldOf("animations").forGetter(Animation::animations)
			).apply(instance, Animation::new)
	);
}
