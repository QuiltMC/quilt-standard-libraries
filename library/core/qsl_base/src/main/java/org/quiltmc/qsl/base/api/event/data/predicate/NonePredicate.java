package org.quiltmc.qsl.base.api.event.data.predicate;

import com.mojang.serialization.Codec;

import net.minecraft.util.Identifier;

/**
 * A predicate that is never true.
 */
public final class NonePredicate<T> implements CodecAwarePredicate<T> {

	public static final Identifier IDENTIFIER = new Identifier("quilt", "none");
	public static final PredicateCodecProvider PROVIDER = NonePredicate::makeCodec;

	@Override
	public boolean test(T t) {
		return false;
	}

	private NonePredicate() {}

	private static <T> Codec<NonePredicate<T>> makeCodec(Codec<CodecAwarePredicate<T>> predicateCodec) {
		return Codec.unit(new NonePredicate<>());
	}

	@Override
	public Identifier getCodecIdentifier() {
		return IDENTIFIER;
	}
}
