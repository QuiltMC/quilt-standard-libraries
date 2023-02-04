package org.quiltmc.qsl.worldgen.biome.api.codec;

import java.util.Arrays;
import java.util.List;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.registry.HolderSet;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.feature.PlacedFeature;

import org.quiltmc.qsl.worldgen.biome.api.BiomeModificationContext;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifier;
import org.quiltmc.qsl.worldgen.biome.api.BiomeSelectionContext;

public record RemoveFeaturesModifier(HolderSet<Biome> biomes, List<RegistryKey<PlacedFeature>> features, List<GenerationStep.Feature> steps) implements BiomeModifier {
	public static final Identifier IDENTIFIER = new Identifier("quilt", "remove_features");
	public static final Codec<RemoveFeaturesModifier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Biome.LIST_CODEC.fieldOf("biomes").forGetter(RemoveFeaturesModifier::biomes),
			Codec.either(RegistryKey.codec(RegistryKeys.PLACED_FEATURE), RegistryKey.codec(RegistryKeys.PLACED_FEATURE).listOf()).xmap(
					either -> either.map(List::of, list -> list),
					list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list)).fieldOf("features").forGetter(RemoveFeaturesModifier::features),
			Codec.either(GenerationStep.Feature.CODEC, GenerationStep.Feature.CODEC.listOf()).xmap(
					either -> either.map(List::of, list -> list),
					list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list)).optionalFieldOf("steps", Arrays.asList(GenerationStep.Feature.values())).forGetter(RemoveFeaturesModifier::steps)
	).apply(instance, RemoveFeaturesModifier::new));

	@Override
	public boolean shouldModify(BiomeSelectionContext context) {
		return biomes.contains(context.getBiomeHolder());
	}

	@Override
	public void modify(BiomeSelectionContext selectionContext, BiomeModificationContext modificationContext) {
		for (var feature : features) {
			for (var step : steps) {
				modificationContext.getGenerationSettings().removeFeature(step, feature);
			}
		}
	}

	@Override
	public Identifier getCodecIdentifier() {
		return IDENTIFIER;
	}
}
