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
