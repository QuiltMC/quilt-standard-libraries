package org.quiltmc.qsl.base.api.event.data.predicate;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;

public final class NotPredicate<T> implements CodecAwarePredicate<T> {

	public static final Identifier IDENTIFIER = new Identifier("quilt", "not");
	public static final PredicateCodecProvider PROVIDER = NotPredicate::makeCodec;

	@Override
	public boolean test(T t) {
		return !value.test(t);
	}

	public final CodecAwarePredicate<T> value;

	private NotPredicate(CodecAwarePredicate<T> value) {
		this.value = value;
	}

	private static <T> Codec<NotPredicate<T>> makeCodec(Codec<CodecAwarePredicate<T>> predicateCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				predicateCodec.fieldOf("value").forGetter(predicate -> predicate.value)
		).apply(instance, NotPredicate::new));
	}

	@Override
	public Identifier getCodecIdentifier() {
		return IDENTIFIER;
	}
}
