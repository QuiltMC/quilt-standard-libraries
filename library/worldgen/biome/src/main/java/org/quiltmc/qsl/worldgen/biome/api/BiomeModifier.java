package org.quiltmc.qsl.worldgen.biome.api;

import java.util.function.Function;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

import net.minecraft.util.dynamic.Codecs;
import net.minecraft.world.biome.Biome;

import org.quiltmc.qsl.base.api.event.data.CodecAware;
import org.quiltmc.qsl.base.api.event.data.CodecMap;
import org.quiltmc.qsl.base.api.event.data.predicate.CodecAwarePredicate;
import org.quiltmc.qsl.base.api.event.data.predicate.PredicateCodecMap;
import org.quiltmc.qsl.worldgen.biome.api.codec.selector.ValueBiomeSelector;

public interface BiomeModifier extends CodecAware {
	CodecMap<BiomeModifier> BIOME_MODIFIER_CODECS = new CodecMap<>();
	Codec<CodecAwarePredicate<BiomeSelectionContext>> BIOME_SELECTOR_CODEC = Codecs.createLazy(() ->
			Codec.either(
					CodecMap.createDelegatingCodec(BiomeModifier.BIOME_SELECTOR_CODECS, "biome selection predicate"),
					Biome.LIST_CODEC).xmap(either -> either.map(Function.identity(), ValueBiomeSelector::new),
					predicate -> predicate instanceof ValueBiomeSelector valueSelector ? Either.right(valueSelector.value()) : Either.left(predicate)));
	PredicateCodecMap<BiomeSelectionContext> BIOME_SELECTOR_CODECS = new PredicateCodecMap<>(BIOME_SELECTOR_CODEC);

	boolean shouldModify(BiomeSelectionContext context);

	void modify(BiomeSelectionContext selectionContext, BiomeModificationContext modificationContext);
}
