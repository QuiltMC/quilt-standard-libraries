/*
 * Copyright 2023 QuiltMC
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
