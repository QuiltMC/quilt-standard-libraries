package org.quiltmc.qsl.base.api.event.data.predicate;

import java.util.Map;

import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.event.data.CodecMap;

public class PredicateCodecMap<T> extends CodecMap<CodecAwarePredicate<T>> {
	private static final Map<Identifier, PredicateCodecProvider> providers = HashBiMap.create();
	public static void registerProvider(Identifier id, PredicateCodecProvider provider) {
		if (providers.containsKey(id)) {
			throw new IllegalArgumentException("Duplicate provider id: " + id);
		}
		providers.put(id, provider);
	}

	public static PredicateCodecProvider lookupProvider(Identifier id) {
		return providers.get(id);
	}

	static {
		registerProvider(AllPredicate.IDENTIFIER, AllPredicate.PROVIDER);
		registerProvider(NonePredicate.IDENTIFIER, NonePredicate.PROVIDER);
		registerProvider(AndPredicate.IDENTIFIER, AndPredicate.PROVIDER);
		registerProvider(OrPredicate.IDENTIFIER, OrPredicate.PROVIDER);
		registerProvider(NotPredicate.IDENTIFIER, NotPredicate.PROVIDER);
	}

	private final Codec<CodecAwarePredicate<T>> predicateCodec;

	public PredicateCodecMap(Codec<CodecAwarePredicate<T>> predicateCodec) {
		super();
		this.predicateCodec = predicateCodec;
	}

	@Override
	public void register(Identifier id, Codec<? extends CodecAwarePredicate<T>> codec) {
		super.register(id, codec);
	}

	@Override
	public Codec<? extends CodecAwarePredicate<T>> lookup(Identifier id) {
		checkCache();
		return super.lookup(id);
	}

	@Override
	public Identifier lookup(Codec<? extends CodecAwarePredicate<T>> codec) {
		checkCache();
		return super.lookup(codec);
	}

	private volatile boolean cached = false;

	private void checkCache() {
		if (!cached) {
			synchronized (this) {
				if (!cached) {
					cacheProviders();
					cached = true;
				}
			}
		}
	}

	private void cacheProviders() {
		for (Map.Entry<Identifier, PredicateCodecProvider> entry : providers.entrySet()) {
			this.register(entry.getKey(), entry.getValue().makeCodec(predicateCodec));
		}
	}
}
