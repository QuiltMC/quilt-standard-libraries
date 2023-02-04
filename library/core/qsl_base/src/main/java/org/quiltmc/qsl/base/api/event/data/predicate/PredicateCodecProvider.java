package org.quiltmc.qsl.base.api.event.data.predicate;

import com.mojang.serialization.Codec;

/**
 * A provider of codecs for {@link CodecAwarePredicate}s. A single provider can provide codecs for predicates parameterized
 * by any type.
 */
public interface PredicateCodecProvider {
	/**
	 * {@return a specific codec for a predicate parameterized by type R}
	 * @param predicateCodec a general codec that can encode any predicate for the specific type R
	 * @param <R> the type of the input tested by the predicate
	 */
	<R> Codec<? extends CodecAwarePredicate<R>> makeCodec(Codec<CodecAwarePredicate<R>> predicateCodec);
}
