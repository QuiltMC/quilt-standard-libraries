/*
 * Copyright 2023 QuiltMC
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

package org.quiltmc.qsl.rendering.entity_models.api.animation;

import java.util.Arrays;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.client.render.animation.Animation;
import net.minecraft.client.render.animation.AnimationKeyframe;
import net.minecraft.client.render.animation.PartAnimation;

import org.quiltmc.qsl.rendering.entity_models.api.Codecs;

/**
 * Codecs for animation loading.
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
