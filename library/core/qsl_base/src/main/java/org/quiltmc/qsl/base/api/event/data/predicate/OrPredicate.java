package org.quiltmc.qsl.base.api.event.data.predicate;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;

/**
 * A predicate that is true if any of its referenced predicates of the same type are true.
 */
public final class OrPredicate<T> implements CodecAwarePredicate<T> {

	public static final Identifier IDENTIFIER = new Identifier("quilt", "or");
	public static final PredicateCodecProvider PROVIDER = OrPredicate::makeCodec;

	@Override
	public boolean test(T t) {
		for (CodecAwarePredicate<T> predicate : values) {
			if (predicate.test(t)) {
				return true;
			}
		}
		return false;
	}

	public final List<CodecAwarePredicate<T>> values;

	private OrPredicate(List<CodecAwarePredicate<T>> values) {
		this.values = values;
	}

	private static <T> Codec<OrPredicate<T>> makeCodec(Codec<CodecAwarePredicate<T>> predicateCodec) {
		return RecordCodecBuilder.create(instance -> instance.group(
				predicateCodec.listOf().fieldOf("values").forGetter(predicate -> predicate.values)
		).apply(instance, OrPredicate::new));
	}

	@Override
	public Identifier getCodecIdentifier() {
		return IDENTIFIER;
	}
}
