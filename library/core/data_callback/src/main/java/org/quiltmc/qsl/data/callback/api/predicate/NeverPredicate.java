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

import net.minecraft.util.Identifier;

/**
 * A predicate that is never true.
 */
public final class NeverPredicate<T> implements CodecAwarePredicate<T> {
	public static final Identifier CODEC_ID = new Identifier("quilt", "never");
	public static final PredicateCodecProvider PROVIDER = NeverPredicate::makeCodec;

	private NeverPredicate() {}

	private static <T> Codec<NeverPredicate<T>> makeCodec(Codec<CodecAwarePredicate<T>> predicateCodec) {
		return Codec.unit(new NeverPredicate<>());
	}

	@Override
	public boolean test(T t) {
		return false;
	}

	@Override
	public Identifier getCodecId() {
		return CODEC_ID;
	}
}
