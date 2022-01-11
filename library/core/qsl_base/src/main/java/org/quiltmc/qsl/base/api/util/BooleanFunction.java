/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.base.api.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Represents a function that accepts a boolean-valued argument and produces a result.
 *
 * <p>This is the {@code boolean}-consuming primitive specialization for {@link java.util.function.Function}.
 */
@FunctionalInterface
public interface BooleanFunction<R> extends Function<Boolean, R> {
	/**
	 * Applies this function to the given argument.
	 *
	 * @param value the function argument
	 * @return the function result
	 */
	R apply(boolean value);

	@Override
	default R apply(Boolean value) {
		return this.apply(value.booleanValue());
	}

	@Override
	@NotNull
	default <V> BooleanFunction<V> andThen(@NotNull Function<? super R, ? extends V> after) {
		Objects.requireNonNull(after);
		return (t) -> after.apply(this.apply(t));
	}

	@NotNull
	default <V> Function<V, R> compose(@NotNull Predicate<? super V> before) {
		Objects.requireNonNull(before);
		return (v) -> this.apply(before.test(v));
	}
}
