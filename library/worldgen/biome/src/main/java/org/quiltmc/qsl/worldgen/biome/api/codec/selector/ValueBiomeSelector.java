package org.quiltmc.qsl.worldgen.biome.api.codec.selector;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.registry.HolderSet;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

import org.quiltmc.qsl.base.api.event.data.predicate.CodecAwarePredicate;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;

/**
 * A biome selector that selects a biome if it is part of the provided holder set; useful for selecting single biomes,
 * lists of biomes, or biome tags.
 */
public record ValueBiomeSelector(HolderSet<Biome> value) implements CodecAwarePredicate<BiomeSelectionContext> {
	public static final Identifier IDENTIFIER = new Identifier("quilt", "value");
	public static final Codec<ValueBiomeSelector> CODEC = RecordCodecBuilder.create(i -> i.group(
			Biome.LIST_CODEC.fieldOf("value").forGetter(ValueBiomeSelector::value)
	).apply(i, ValueBiomeSelector::new));


	@Override
	public boolean test(BiomeSelectionContext biomeSelectionContext) {
		return value.contains(biomeSelectionContext.getBiomeHolder());
	}

	@Override
	public Identifier getCodecIdentifier() {
		return IDENTIFIER;
	}
}
