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

package org.quiltmc.qsl.data.callbacks.predicate;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.util.Identifier;

/**
 * A predicate that is true if all of its referenced predicates of the same type are true.
 */
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
