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

/**
 * Represents a modification that can be applied to a biome.
 */
public interface BiomeModifier extends CodecAware {
	/**
	 * Stores biome modifier codecs. Custom biome modifier codecs should be added here during mod initialization.
	 */
	CodecMap<BiomeModifier> BIOME_MODIFIER_CODECS = new CodecMap<>();
	Codec<CodecAwarePredicate<BiomeSelectionContext>> BIOME_SELECTOR_CODEC = Codecs.createLazy(() ->
			Codec.either(
					BiomeModifier.BIOME_SELECTOR_CODECS.createDelegatingCodec("biome selection predicate"),
					Biome.LIST_CODEC).xmap(either -> either.map(Function.identity(), ValueBiomeSelector::new),
					predicate -> predicate instanceof ValueBiomeSelector valueSelector ? Either.right(valueSelector.value()) : Either.left(predicate)));
	/**
	 * Stores biome selector codecs. Custom biome selector codecs should be added here during mod initialization.
	 */
	PredicateCodecMap<BiomeSelectionContext> BIOME_SELECTOR_CODECS = new PredicateCodecMap<>(BIOME_SELECTOR_CODEC);

	/**
	 * {@return whether this biome modifier should be applied to the biome}
	 */
	boolean shouldModify(BiomeSelectionContext context);

	/**
	 * Applies this biome modifier to the biome.
	 */
	void modify(BiomeSelectionContext selectionContext, BiomeModificationContext modificationContext);
}
