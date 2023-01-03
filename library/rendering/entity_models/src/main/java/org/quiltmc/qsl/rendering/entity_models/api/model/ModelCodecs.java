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

import java.util.HashMap;
import java.util.Optional;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.joml.Vector3f;
import org.quiltmc.qsl.rendering.entity_models.api.Codecs;
import org.quiltmc.qsl.rendering.entity_models.impl.LazyTypeUnboundedMapCodec;
import org.quiltmc.qsl.rendering.entity_models.mixin.DilationAccessor;
import org.quiltmc.qsl.rendering.entity_models.mixin.ModelCuboidDataAccessor;
import org.quiltmc.qsl.rendering.entity_models.mixin.ModelPartDataAccessor;
import org.quiltmc.qsl.rendering.entity_models.mixin.TextureDimensionsAccessor;
import org.quiltmc.qsl.rendering.entity_models.mixin.TexturedModelDataAccessor;

import net.minecraft.client.model.Dilation;
import net.minecraft.client.model.ModelCuboidData;
import net.minecraft.client.model.ModelData;
import net.minecraft.client.model.ModelPartData;
import net.minecraft.client.model.ModelTransform;
import net.minecraft.client.model.TextureDimensions;
import net.minecraft.client.model.TexturedModelData;
import net.minecraft.client.util.math.Vector2f;
import net.minecraft.util.Util;

/**
 * Codecs for model loading.
 */
public final class ModelCodecs {
	public static final Codec<TextureDimensions> TEXTURE_DIMENSIONS = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.INT.fieldOf("width").forGetter(obj -> ((TextureDimensionsAccessor) obj).width()),
					Codec.INT.fieldOf("height").forGetter(obj -> ((TextureDimensionsAccessor) obj).height())
			).apply(instance, TextureDimensions::new)
	);

	public static final Codec<ModelTransform> MODEL_TRANSFORM = RecordCodecBuilder.create(instance ->
			instance.group(
					Codecs.VECTOR_3F_CODEC.optionalFieldOf("origin", new Vector3f()).forGetter(obj -> new Vector3f(obj.pivotX, obj.pivotY, obj.pivotZ)),
					Codecs.VECTOR_3F_CODEC.optionalFieldOf("rotation", new Vector3f()).forGetter(obj -> new Vector3f(obj.pitch, obj.yaw, obj.roll))
			).apply(instance, (origin, rot) -> ModelTransform.of(origin.x(), origin.y(), origin.z(), rot.x(), rot.y(), rot.z()))
	);

	public static final Codec<Dilation> DILATION = Codecs.VECTOR_3F_CODEC.xmap(
			vec -> new Dilation(vec.x(), vec.y(), vec.z()),
			dil -> new Vector3f(
					((DilationAccessor) dil).radiusX(),
					((DilationAccessor) dil).radiusY(),
					((DilationAccessor) dil).radiusZ())
	);

	public static final Codec<Vector2f> VECTOR2F = Codec.FLOAT.listOf().comapFlatMap(vec ->
					Util.fixedSizeList(vec, 2).map((arr) -> new Vector2f(arr.get(0), arr.get(1))),
			vec -> ImmutableList.of(vec.getX(), vec.getY())
	);

	private static ModelCuboidData createCuboidData(Optional<String> name, Vector3f offset, Vector3f dimensions, Dilation dilation, boolean mirror, Vector2f uv, Vector2f uvSize) {
		return ModelCuboidDataAccessor.create(
				name.orElse(null),
				uv.getX(), uv.getY(),
				offset.x(), offset.y(), offset.z(),
				dimensions.x(), dimensions.y(), dimensions.z(),
				dilation,
				mirror,
				uvSize.getX(), uvSize.getY());
	}

	private static final Vector2f DEFAULT_UV_SCALE = new Vector2f(1.0f, 1.0f);

	public static final Codec<ModelCuboidData> MODEL_CUBOID_DATA = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.STRING.optionalFieldOf("name").forGetter(obj -> Optional.ofNullable(((ModelCuboidDataAccessor) (Object) obj).name())),
					Codecs.VECTOR_3F_CODEC.fieldOf("offset").forGetter(obj -> ((ModelCuboidDataAccessor) (Object) obj).offset()),
					Codecs.VECTOR_3F_CODEC.fieldOf("dimensions").forGetter(obj -> ((ModelCuboidDataAccessor) (Object) obj).dimensions()),
					DILATION.optionalFieldOf("dilation", Dilation.NONE).forGetter(obj -> ((ModelCuboidDataAccessor) (Object) obj).dilation()),
					Codec.BOOL.optionalFieldOf("mirror", false).forGetter(obj -> ((ModelCuboidDataAccessor) (Object) obj).mirror()),
					VECTOR2F.fieldOf("uv").forGetter(obj -> ((ModelCuboidDataAccessor) (Object) obj).uv()),
					VECTOR2F.optionalFieldOf("uv_scale", DEFAULT_UV_SCALE).forGetter(obj -> ((ModelCuboidDataAccessor) (Object) obj).uvScale())
			).apply(instance, ModelCodecs::createCuboidData)
	);

	public static final Codec<ModelPartData> MODEL_PART_DATA = RecordCodecBuilder.create(instance ->
			instance.group(
					MODEL_TRANSFORM.optionalFieldOf("transform", ModelTransform.NONE).forGetter(obj -> ((ModelPartDataAccessor) obj).transform()),
					Codec.list(MODEL_CUBOID_DATA).fieldOf("cuboids").forGetter(obj -> ((ModelPartDataAccessor) obj).cuboids()),
					LazyTypeUnboundedMapCodec.of(Codec.STRING, () -> ModelCodecs.MODEL_PART_DATA).optionalFieldOf("children", new HashMap<>()).forGetter(obj -> ((ModelPartDataAccessor) obj).children())
			).apply(instance, (transform, cuboids, children) -> {
				var data = ModelPartDataAccessor.create(cuboids, transform);
				((ModelPartDataAccessor) data).children().putAll(children);
				return data;
			})
	);

	public static final Codec<TexturedModelData> TEXTURED_MODEL_DATA = RecordCodecBuilder.create(instance ->
			instance.group(
					TEXTURE_DIMENSIONS.fieldOf("texture").forGetter(obj -> ((TexturedModelDataAccessor) obj).texture()),
					Codec.unboundedMap(Codec.STRING, MODEL_PART_DATA).fieldOf("bones").forGetter(obj -> ((ModelPartDataAccessor) ((TexturedModelDataAccessor) obj).root().getRoot()).children())
			).apply(instance, (texture, bones) -> {
				var data = new ModelData();
				((ModelPartDataAccessor) data.getRoot()).children().putAll(bones);
				return TexturedModelDataAccessor.create(data, texture);
			})
	);
}
