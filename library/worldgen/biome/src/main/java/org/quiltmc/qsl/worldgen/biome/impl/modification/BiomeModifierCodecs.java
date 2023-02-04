package org.quiltmc.qsl.worldgen.biome.impl.modification;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.worldgen.biome.api.BiomeModifier;
import org.quiltmc.qsl.worldgen.biome.api.codec.AddFeaturesModifier;
import org.quiltmc.qsl.worldgen.biome.api.codec.NothingModifier;
import org.quiltmc.qsl.worldgen.biome.api.codec.RemoveFeaturesModifier;
import org.quiltmc.qsl.worldgen.biome.api.codec.selector.ValueBiomeSelector;

public class BiomeModifierCodecs implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
		BiomeModifier.BIOME_MODIFIER_CODECS.register(NothingModifier.IDENTIFIER, NothingModifier.CODEC);
		BiomeModifier.BIOME_MODIFIER_CODECS.register(AddFeaturesModifier.IDENTIFIER, AddFeaturesModifier.CODEC);
		BiomeModifier.BIOME_MODIFIER_CODECS.register(RemoveFeaturesModifier.IDENTIFIER, RemoveFeaturesModifier.CODEC);

		BiomeModifier.BIOME_SELECTOR_CODECS.register(ValueBiomeSelector.IDENTIFIER, ValueBiomeSelector.CODEC);
	}
}
