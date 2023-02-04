package org.quiltmc.qsl.base.api.event.data.predicate;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;

public final class AndPredicate<T> implements CodecAwarePredicate<T> {

	public static final Identifier IDENTIFIER = new Identifier("quilt", "and");
	public static final PredicateCodecProvider PROVIDER = AndPredicate::makeCodec;

	@Override
	public boolean test(T t) {
		for (CodecAwarePredicate<T> predicate : values) {
			if (!predicate.test(t)) {
				return false;
			}
		}
		return true;
	}

	public final List<CodecAwarePredicate<T>> values;

	private AndPredicate(List<CodecAwarePredicate<T>> values) {
		this.values = values;
	}

	private static <T> Codec<AndPredicate<T>> makeCodec(Codec<CodecAwarePredicate<T>> predicateCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				predicateCodec.listOf().fieldOf("values").forGetter(predicate -> predicate.values)
		).apply(instance, AndPredicate::new));
	}

	@Override
	public Identifier getCodecIdentifier() {
		return IDENTIFIER;
	}
}
