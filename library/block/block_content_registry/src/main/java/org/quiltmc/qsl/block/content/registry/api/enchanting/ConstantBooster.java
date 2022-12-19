package org.quiltmc.qsl.block.content.registry.api.enchanting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

/**
 * A constant booster.
 *
 * @param value The boost level
 */
public record ConstantBooster(float value) implements EnchantingBooster {
	public static final Codec<ConstantBooster> CODEC = RecordCodecBuilder.create(instance ->
			instance.group(
					Codec.FLOAT.fieldOf("value").forGetter(ConstantBooster::value)
			).apply(instance, ConstantBooster::new)
	);
	public static EnchantingBoosterType CONSTANT = EnchantingBoosters.register(new Identifier("quilt", "constant"), ConstantBooster.CODEC);

	@Override
	public float getEnchantingBoost(World world, BlockState state, BlockPos pos) {
		return value;
	}

	@Override
	public EnchantingBoosterType getType() {
		return CONSTANT;
	}
}
