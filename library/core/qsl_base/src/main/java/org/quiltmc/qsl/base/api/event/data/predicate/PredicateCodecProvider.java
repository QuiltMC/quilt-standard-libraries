package org.quiltmc.qsl.base.api.event.data.predicate;

import com.mojang.serialization.Codec;

public interface PredicateCodecProvider {
	<R> Codec<? extends CodecAwarePredicate<R>> makeCodec(Codec<CodecAwarePredicate<R>> predicateCodec);
}
