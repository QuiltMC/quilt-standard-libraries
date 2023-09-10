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

package org.quiltmc.qsl.rendering.entity_models.api.model;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.client.model.TexturedModelData;
import net.minecraft.util.Identifier;

public class ModelTypes {
	private static final BiMap<Identifier, ModelType> TYPES = HashBiMap.create();

	public static final ModelType QUILT_MODEL = register("quilt:model", ModelCodecs.TEXTURED_MODEL_DATA);

	public static final Codec<ModelType> TYPE_CODEC = Identifier.CODEC.flatXmap(identifier -> {
		ModelType type = TYPES.get(identifier);
		return type != null ? DataResult.success(type) : DataResult.error(() -> "Unknown model type: " + identifier);
	}, model -> {
		Identifier id = TYPES.inverse().get(model);
		return id != null ? DataResult.success(id) : DataResult.error(() -> "Unknown model type.");
	});
	public static Codec<TexturedModelData> CODEC = TYPE_CODEC.dispatch(model -> ((TypedModel) (Object) model).getType(), ModelType::codec);

	public static ModelType register(String name, Codec<TexturedModelData> codec) {
		return register(new Identifier(name), codec);
	}

	public static ModelType register(Identifier id, Codec<TexturedModelData> codec) {
		ModelType type = new ModelType(codec);
		ModelType old = TYPES.putIfAbsent(id, type);
		if (old != null) {
			throw new IllegalStateException("Duplicate registration for " + id);
		}

		return type;
	}
}
