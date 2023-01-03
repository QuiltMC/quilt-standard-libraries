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
