/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.component.impl.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Lazy<T> {
	@NotNull
	private final Supplier<T> sup;
	@Nullable
	private T value;

	private Lazy(@NotNull Supplier<T> sup) {
		this.sup = sup;
		this.value = null;
	}

	private Lazy(@NotNull T value) {
		this.value = value;
		this.sup = () -> value;
	}

	@NotNull
	public static <T> Lazy<T> filled(@NotNull T value) {
		return new Lazy<>(value);
	}

	@NotNull
	public <U> Lazy<U> compose(Function<T, U> transformer) {
		return Lazy.of(() -> transformer.apply(this.get()));
	}

	@NotNull
	public static <T> Lazy<T> of(@NotNull Supplier<T> sup) {
		return new Lazy<>(sup);
	}

	@NotNull
	public Supplier<T> getSupplier() {
		return sup;
	}

	@NotNull
	public <U> Lazy<U> map(@NotNull Function<T, U> func) {
		return Lazy.of(() -> func.apply(this.get()));
	}

	@NotNull
	public T get() {
		if (value == null) {
			this.value = sup.get();
		}

		return value;
	}

	public void ifPresent(@NotNull Consumer<T> action) {
		if (!this.isEmpty()) {
			action.accept(this.get());
		}
	}

	public boolean isEmpty() {
		return this.value == null;
	}

	public void compute(@NotNull Consumer<T> action) {
		action.accept(this.get());
	}
}
