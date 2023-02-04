package org.quiltmc.qsl.base.api.event.data.predicate;

import com.mojang.serialization.Codec;

import net.minecraft.util.Identifier;

public final class AllPredicate<T> implements CodecAwarePredicate<T> {

	public static final Identifier IDENTIFIER = new Identifier("quilt", "all");
	public static final PredicateCodecProvider PROVIDER = AllPredicate::makeCodec;

	@Override
	public boolean test(T t) {
		return true;
	}

	private AllPredicate() {}

	private static <T> Codec<AllPredicate<T>> makeCodec(Codec<CodecAwarePredicate<T>> predicateCodec) {
		return Codec.unit(new AllPredicate<>());
	}

	@Override
	public Identifier getCodecIdentifier() {
		return IDENTIFIER;
	}
}
