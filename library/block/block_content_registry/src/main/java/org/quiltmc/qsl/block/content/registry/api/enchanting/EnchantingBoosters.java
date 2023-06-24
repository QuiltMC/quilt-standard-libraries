/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.block.content.registry.api.enchanting;

import java.util.Optional;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.util.Identifier;

/**
 * The class responsible for managing {@link EnchantingBooster}s.
 */
public class EnchantingBoosters {
	private static final BiMap<Identifier, EnchantingBoosterType> TYPES = HashBiMap.create();

	/**
	 * The codec for {@link EnchantingBoosterType}s.
	 */
	public static Codec<EnchantingBoosterType> TYPE_CODEC = Identifier.CODEC.flatXmap(id -> {
		EnchantingBoosterType type = TYPES.get(id);
		return type != null ? DataResult.success(type) : DataResult.error(() -> "Unknown enchanting booster type: " + id);
	}, type -> {
		Identifier identifier = TYPES.inverse().get(type);
		return identifier != null ? DataResult.success(identifier) : DataResult.error(() -> "Unknown enchanting booster type");
	});

	private static final Codec<Either<Either<Float, Identifier>, EnchantingBooster>> EITHER_CODEC = Codec.either(
			Codec.either(Codec.FLOAT, Identifier.CODEC),
			TYPE_CODEC.dispatch(EnchantingBooster::getType, EnchantingBoosterType::codec)
	);
	/**
	 * The codec for an {@link EnchantingBooster}.
	 */
	public static final Codec<EnchantingBooster> CODEC = EITHER_CODEC
			.flatXmap(either ->
							either.<DataResult<EnchantingBooster>>map(floatId ->
											floatId.map(f -> DataResult.success(new ConstantBooster(f)), id -> {
												EnchantingBoosterType type = TYPES.get(id);
												if (type == null) {
													return DataResult.error(() -> "Unknown Booster Type: " + id);
												}

												return type.simpleVariant().isPresent()
														? DataResult.success(type.simpleVariant().get())
														: DataResult.<EnchantingBooster>error(() -> "Booster Type: " + id + " is not simple. Please fully specify it.");
											}),
									DataResult::success),
					enchantingBooster -> {
						if (enchantingBooster instanceof ConstantBooster constant) {
							return DataResult.success(Either.left(Either.left(constant.value())));
						}

						if (enchantingBooster.getType().simpleVariant().isPresent() && enchantingBooster.getType().simpleVariant().get().equals(enchantingBooster)) {
							return DataResult.success(Either.left(Either.right(TYPES.inverse().get(enchantingBooster.getType()))));
						}

						return DataResult.success(Either.right(enchantingBooster));
					});

	/**
	 * Registers a non-simple booster type.
	 *
	 * @param id    the booster type id
	 * @param codec the codec for the booster
	 * @return the type for the booster
	 */
	public static EnchantingBoosterType register(Identifier id, Codec<? extends EnchantingBooster> codec) {
		var type = new EnchantingBoosterType(codec, Optional.empty());
		return register(id, type);
	}

	/**
	 * Registers a booster type.
	 *
	 * @param id   the booster type id
	 * @param type the type for the booster
	 * @return {@code type}
	 */
	public static EnchantingBoosterType register(Identifier id, EnchantingBoosterType type) {
		if (TYPES.containsKey(id)) {
			throw new IllegalArgumentException(id + " already used as name");
		} else if (TYPES.containsValue(type)) {
			throw new IllegalArgumentException("Type already assigned to " + TYPES.inverse().get(type));
		}

		TYPES.put(id, type);
		return type;
	}
}
