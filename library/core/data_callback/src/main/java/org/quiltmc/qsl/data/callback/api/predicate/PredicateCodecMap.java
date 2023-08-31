/*
 * Copyright 2023 The Quilt Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.data.callback.api.predicate;

import java.util.Map;
import java.util.function.Supplier;

import com.google.common.collect.HashBiMap;
import com.mojang.serialization.Codec;

import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;

import org.quiltmc.qsl.data.callback.api.CodecMap;

/**
 * A CodecMap for {@link CodecAwarePredicate}s that bundles a set of codecs created by shared providers. This class
 * also handles registering shared providers for predicate codecs.
 *
 * @param <T> the type of the input tested by the predicates this map can help encode
 */
public class PredicateCodecMap<T> extends CodecMap<CodecAwarePredicate<T>> {
	private static final Map<Identifier, PredicateCodecProvider> PROVIDERS = HashBiMap.create();

	static {
		registerProvider(AlwaysPredicate.CODEC_ID, AlwaysPredicate.PROVIDER);
		registerProvider(NeverPredicate.CODEC_ID, NeverPredicate.PROVIDER);
		registerProvider(AndPredicate.CODEC_ID, AndPredicate.PROVIDER);
		registerProvider(OrPredicate.CODEC_ID, OrPredicate.PROVIDER);
		registerProvider(NotPredicate.CODEC_ID, NotPredicate.PROVIDER);
	}

	private final Codec<CodecAwarePredicate<T>> predicateCodec;
	private volatile boolean cached = false;

	/**
	 * Create a new predicate codec map based off of the provided general predicate codec. The provided codec should
	 * likely be created with {@link Codecs#createLazy(Supplier)} so that it can itself delegate to the constructed map.
	 *
	 * @param predicateCodec a general codec that can encode any predicate for the specific type T
	 */
	public PredicateCodecMap(Codec<CodecAwarePredicate<T>> predicateCodec) {
		super();
		this.predicateCodec = predicateCodec;
	}

	/**
	 * Register a general predicate codec provider. This provider will be used in every predicate codec map to generate
	 * a specifically parameterized codec.
	 *
	 * @param id       the identifier to delegate to this provider on
	 * @param provider the provider to register
	 * @throws IllegalArgumentException if a provider with the same id has already been registered
	 */
	public static void registerProvider(Identifier id, PredicateCodecProvider provider) {
		if (PROVIDERS.containsKey(id)) {
			throw new IllegalArgumentException("Duplicate provider id: " + id);
		}

		PROVIDERS.put(id, provider);
	}

	/**
	 * {@return the provider registered to the given id, or null if no provider has been registered}
	 *
	 * @param id the id to look up
	 */
	public static PredicateCodecProvider lookupProvider(Identifier id) {
		return PROVIDERS.get(id);
	}

	@Override
	public void register(Identifier id, Codec<? extends CodecAwarePredicate<T>> codec) {
		super.register(id, codec);
	}

	@Override
	public Codec<? extends CodecAwarePredicate<T>> lookup(Identifier id) {
		this.checkCache();
		return super.lookup(id);
	}

	@Override
	public Identifier lookup(Codec<? extends CodecAwarePredicate<T>> codec) {
		this.checkCache();
		return super.lookup(codec);
	}

	private void checkCache() {
		if (!this.cached) {
			synchronized (this) {
				if (!this.cached) {
					this.cacheProviders();
					this.cached = true;
				}
			}
		}
	}

	private void cacheProviders() {
		for (Map.Entry<Identifier, PredicateCodecProvider> entry : PROVIDERS.entrySet()) {
			this.register(entry.getKey(), entry.getValue().makeCodec(this.predicateCodec));
		}
	}
}
