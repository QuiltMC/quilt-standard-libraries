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

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.client.render.animation.PartAnimation;
import net.minecraft.util.Identifier;

/**
 * A for registering different {@link net.minecraft.client.render.animation.PartAnimation.Transformation}s to.
 */
public class AnimationTransformations {
	private static final BiMap<Identifier, PartAnimation.Transformation> TRANSFORMATIONS = HashBiMap.create();

	public static final Codec<PartAnimation.Transformation> CODEC = Identifier.CODEC.flatXmap(identifier -> {
		PartAnimation.Transformation type = TRANSFORMATIONS.get(identifier);
		return type != null ? DataResult.success(type) : DataResult.error(() -> "Unknown transformation: " + identifier);
	}, type -> {
		Identifier id = TRANSFORMATIONS.inverse().get(type);
		return id != null ? DataResult.success(id) : DataResult.error(() -> "Unknown transformation.");
	});

	static {
		register("translate", PartAnimation.AnimationTargets.TRANSLATE);
		register("rotate", PartAnimation.AnimationTargets.ROTATE);
		register("scale", PartAnimation.AnimationTargets.SCALE);
	}

	public static void register(String name, PartAnimation.Transformation transformation) {
		register(new Identifier(name), transformation);
	}

	public static void register(Identifier id, PartAnimation.Transformation transformation) {
		if (TRANSFORMATIONS.containsKey(id)) {
			throw new IllegalArgumentException(id + " already used as name");
		} else if (TRANSFORMATIONS.containsValue(transformation)) {
			throw new IllegalArgumentException("Transformation already assigned to " + TRANSFORMATIONS.inverse().get(transformation));
		}

		TRANSFORMATIONS.put(id, transformation);
	}
}
