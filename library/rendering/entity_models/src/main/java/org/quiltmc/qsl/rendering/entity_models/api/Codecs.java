package org.quiltmc.qsl.rendering.entity_models.api;

import java.util.Arrays;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.render.animation.Animation;
import net.minecraft.client.render.animation.AnimationKeyframe;
import net.minecraft.client.render.animation.PartAnimation;
import net.minecraft.util.math.Vec3f;

public class Codecs {
	public static final Codec<AnimationKeyframe> KEYFRAME = RecordCodecBuilder.create(instance -> instance.group(
			Codec.floatRange(0, Float.MAX_VALUE).fieldOf("timestamp").forGetter(AnimationKeyframe::timestamp),
			Vec3f.CODEC.fieldOf("transformation").forGetter(AnimationKeyframe::transformation),
			Codec.STRING.flatXmap(
					s -> Interpolators.get(s).map(DataResult::success).orElseGet(() -> DataResult.error("Unknown interpolator: " + s)),
					i -> Interpolators.get(i).map(DataResult::success).orElse(DataResult.error("Unknown interpolator"))
			).fieldOf("interpolator").forGetter(AnimationKeyframe::interpolator)
	).apply(instance, AnimationKeyframe::new));

	public static final Codec<PartAnimation> PART_ANIMATION = RecordCodecBuilder.create(instance -> instance.group(
			Codec.STRING.flatXmap(s -> switch (s) {
				case "TRANSLATE" -> DataResult.success(PartAnimation.AnimationTargets.TRANSLATE);
				case "ROTATE" -> DataResult.success(PartAnimation.AnimationTargets.ROTATE);
				case "SCALE" -> DataResult.success(PartAnimation.AnimationTargets.SCALE);
				default -> DataResult.error("Unknown transformation: " + s);
			}, transformation -> {
				if (transformation == PartAnimation.AnimationTargets.TRANSLATE) {
					return DataResult.success("TRANSLATE");
				} else if (transformation == PartAnimation.AnimationTargets.ROTATE) {
					return DataResult.success("ROTATE");
				} else if (transformation == PartAnimation.AnimationTargets.SCALE) {
					return DataResult.success("SCALE");
				} else {
					return DataResult.error("Unknown transformation");
				}
			}).fieldOf("transformation").forGetter(PartAnimation::transformation),
			Codec.list(KEYFRAME).xmap(list -> list.toArray(AnimationKeyframe[]::new), Arrays::asList).fieldOf("keyframes").forGetter(PartAnimation::keyframes)
	).apply(instance, PartAnimation::new));

	public static final Codec<Animation> ANIMATION = RecordCodecBuilder.create(instance -> instance.group(
			Codec.floatRange(0, Float.MAX_VALUE).fieldOf("length").forGetter(Animation::length),
			Codec.BOOL.fieldOf("looping").forGetter(Animation::looping),
			Codec.unboundedMap(Codec.STRING, Codec.list(PART_ANIMATION)).fieldOf("animations").forGetter(Animation::animations)
	).apply(instance, Animation::new));
}
