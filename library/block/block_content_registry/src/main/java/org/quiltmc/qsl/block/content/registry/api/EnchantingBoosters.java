package org.quiltmc.qsl.block.content.registry.api;

import java.util.function.Function;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * The class responsible for managing {@link EnchantingBooster}s
 */
public class EnchantingBoosters {
	private static final BiMap<Identifier, EnchantingBoosterType> TYPES = HashBiMap.create();

	/**
	 * The codec for {@link EnchantingBoosterType}s
	 */
	public static Codec<EnchantingBoosterType> TYPE_CODEC = Identifier.CODEC.flatXmap(id -> {
		EnchantingBoosterType type = TYPES.get(id);
		return type != null ? DataResult.success(type) : DataResult.error("Unknown enchanting booster type: " + id);
	}, type -> {
		Identifier identifier = TYPES.inverse().get(type);
		return identifier != null ? DataResult.success(identifier) : DataResult.error("Unknown enchanting booster type");
	});

	private static final Codec<Either<Either<Float, Identifier>, EnchantingBooster>> EITHER_CODEC = Codec.either(
			Codec.either(Codec.floatRange(0, Float.MAX_VALUE), Identifier.CODEC),
			TYPE_CODEC.dispatch(EnchantingBooster::getType, EnchantingBoosterType::codec)
	);
	/**
	 * The codec for an {@link EnchantingBooster}
	 */
	public static final Codec<EnchantingBooster> CODEC = EITHER_CODEC
			.flatXmap(either ->
						either.<DataResult<EnchantingBooster>>map(floatId ->
							floatId.map(f -> DataResult.success(new ConstantBooster(f)), id -> {
								EnchantingBoosterType type = TYPES.get(id);
								if (type == null) {
									return DataResult.error("Unknown Booster Type: " + id);
								}

								return type.isSimple() ? DataResult.success(type.simple()) : DataResult.<EnchantingBooster>error("Booster Type: " + id + " is not simple. Please fully specify it.");
							}),
							DataResult::success),
					enchantingBooster -> {
						if (enchantingBooster instanceof ConstantBooster constant) {
							return DataResult.success(Either.left(Either.left(constant.value())));
						}

						if (enchantingBooster.getType().isSimple() && enchantingBooster.getType().simple() == enchantingBooster) {
							return DataResult.success(Either.left(Either.right(TYPES.inverse().get(enchantingBooster.getType()))));
						}

						return DataResult.success(Either.right(enchantingBooster));
					});


	/**
	 * The interface in charge of calculating the enchanting boost value
	 */
	public interface EnchantingBooster {
		/**
		 * Gets the current boost level for the given parameter
		 * @param world The current world
		 * @param state The block state
		 * @param pos The position of the block
		 * @return The boost level
		 */
		float getEnchantingBoost(World world, BlockState state, BlockPos pos);

		/**
		 * @return The type for this booster
		 */
		EnchantingBoosterType getType();
	}

	/**
	 * A type to identify booster variations by
	 * @param codec The codec for the booster
	 * @param isSimple If the type is simple, meaning that it can be identified by solely its identifier
	 * @param simple The booster when encoded from its identifier
	 */
	public record EnchantingBoosterType(Codec<? extends EnchantingBooster> codec, boolean isSimple, @Nullable EnchantingBooster simple) {
	}

	/**
	 * Registers a non-simple booster type
	 * @param id The booster type id
	 * @param codec The codec for the booster
	 * @return The type for the booster
	 */
	public static EnchantingBoosterType register(Identifier id, Codec<? extends EnchantingBooster> codec) {
		EnchantingBoosterType type = new EnchantingBoosterType(codec, false, null);
		return register(id, type);
	}

	/**
	 * Registers a booster type
	 * @param id The booster type id
	 * @param type The type for the booster
	 * @return {@code type}
	 */
	private static EnchantingBoosterType register(Identifier id, EnchantingBoosterType type) {
		if (TYPES.containsKey(id)) {
			throw new IllegalArgumentException(id + " already used as name");
		} else if (TYPES.containsValue(type)) {
			throw new IllegalArgumentException("Type already assigned to " + TYPES.inverse().get(type));
		}

		TYPES.put(id, type);
		return type;
	}

	/**
	 * A constant booster
	 * @param value The boost level
	 */
	public record ConstantBooster(float value) implements EnchantingBooster {
		public static EnchantingBoosterType CONSTANT = register(new Identifier("quilt", "constant"), ConstantBooster.CODEC);

		public static final Codec<ConstantBooster> CODEC = RecordCodecBuilder.create(instance ->
				instance.group(
						Codec.FLOAT.fieldOf("value").forGetter(ConstantBooster::value)
				).apply(instance, ConstantBooster::new)
		);

		@Override
		public float getEnchantingBoost(World world, BlockState state, BlockPos pos) {
			return value;
		}

		@Override
		public EnchantingBoosterType getType() {
			return CONSTANT;
		}
	}
}
