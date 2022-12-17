/*
 * Copyright 2022 QuiltMC
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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.client.render.animation.PartAnimation;
import net.minecraft.util.Identifier;

/**
 * A class for registering different {@link net.minecraft.client.render.animation.PartAnimation.Interpolator}s to
 */
public class AnimationInterpolations {
	private static final BiMap<Identifier, PartAnimation.Interpolator> INTERPOLATORS = HashBiMap.create();

	public static final Codec<PartAnimation.Interpolator> CODEC = Identifier.CODEC.flatXmap(identifier -> {
		PartAnimation.Interpolator type = INTERPOLATORS.get(identifier);
		return type != null ? DataResult.success(type) : DataResult.error("Unknown interpolator: " + identifier);
	}, type -> {
		Identifier id = INTERPOLATORS.inverse().get(type);
		return id != null ? DataResult.success(id) : DataResult.error("Unknown interpolator.");
	});

	static {
		register("linear", PartAnimation.Interpolators.LINEAR);
		register("spline", PartAnimation.Interpolators.SPLINE);
	}

	public static void register(String name, PartAnimation.Interpolator interpolator) {
		register(new Identifier(name), interpolator);
	}

	public static void register(Identifier id, PartAnimation.Interpolator interpolator) {
		if (INTERPOLATORS.containsKey(id)) {
			throw new IllegalArgumentException(id + " already used as name");
		} else if (INTERPOLATORS.containsValue(interpolator)) {
			throw new IllegalArgumentException("Interpolator already assigned to " + INTERPOLATORS.inverse().get(interpolator));
		}

		INTERPOLATORS.put(id, interpolator);
	}
}
