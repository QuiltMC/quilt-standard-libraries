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

package org.quiltmc.qsl.data.callback.api;

import java.util.List;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;

/**
 * Helper methods for common codec operations.
 */
public final class CodecHelpers {
	private CodecHelpers() {}

	/**
	 * {@return a codec that encodes a list of values either from a list or from a single value}
	 */
	public static <A> Codec<List<A>> listOrValue(Codec<A> codec) {
		return Codec.either(codec, codec.listOf()).xmap(
				either -> either.map(List::of, list -> list),
				list -> list.size() == 1 ? Either.left(list.get(0)) : Either.right(list));
	}
}
