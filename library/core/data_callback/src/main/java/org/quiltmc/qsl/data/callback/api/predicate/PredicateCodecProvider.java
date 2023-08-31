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

import com.mojang.serialization.Codec;

/**
 * A provider of codecs for {@link CodecAwarePredicate}s. A single provider can provide codecs for predicates parameterized
 * by any type.
 */
@FunctionalInterface
public interface PredicateCodecProvider {
	/**
	 * {@return a specific codec for a predicate parameterized by type R}
	 *
	 * @param predicateCodec a general codec that can encode any predicate for the specific type R
	 * @param <R>            the type of the input tested by the predicate
	 */
	<R> Codec<? extends CodecAwarePredicate<R>> makeCodec(Codec<CodecAwarePredicate<R>> predicateCodec);
}
