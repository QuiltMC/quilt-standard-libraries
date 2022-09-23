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

package org.quiltmc.qsl.rendering.entity_models.api;

import java.util.Optional;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import net.minecraft.client.render.animation.PartAnimation;

/**
 * A class that stores a {@link BiMap} of strings to {@link PartAnimation.Interpolator}s
 */
public class AnimationUtils {
	private static final BiMap<String, PartAnimation.Interpolator> INTERPOLATORS = HashBiMap.create();

	static {
		registerInterpolation("LINEAR", PartAnimation.Interpolators.LINEAR);
		registerInterpolation("SPLINE", PartAnimation.Interpolators.SPLINE);
	}

	public static void registerInterpolation(String name, PartAnimation.Interpolator interpolator) {
		if (INTERPOLATORS.containsKey(name)) {
			throw new IllegalArgumentException(name + " already used as name");
		} else if (INTERPOLATORS.containsValue(interpolator)) {
			throw new IllegalArgumentException("Interpolator already assigned to " + INTERPOLATORS.inverse().get(interpolator));
		}

		INTERPOLATORS.put(name, interpolator);
	}

	public static Optional<PartAnimation.Interpolator> getInterpolatorFromName(String name) {
		return Optional.ofNullable(INTERPOLATORS.get(name));
	}

	public static Optional<String> getNameForInterpolator(PartAnimation.Interpolator interpolator) {
		return Optional.ofNullable(INTERPOLATORS.inverse().get(interpolator));
	}
}
