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

package org.quiltmc.qsl.base.api.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a value that is only initialized once it's needed.
 *
 * <p>
 * Similar to {@link com.google.common.base.Suppliers#memoize}.
 *
 * @param <T> the type of the value this {@link Lazy} represents
 */
public abstract sealed class Lazy<T> implements Supplier<T> permits Lazy.Filled, Lazy.OfSupplier {
	/**
	 * Wraps the provided {@link Supplier} into a {@link OfSupplier} instance.
	 *
	 * @param supplier the {@link Supplier} to a value
	 * @param <T>      The type of the wrapped value
	 * @return a new {@link OfSupplier} instance
	 */
	public static <T> Lazy.OfSupplier<T> of(Supplier<? extends T> supplier) {
		return new OfSupplier<>(supplier);
	}

	/**
	 * Returns an {@link Filled} instance wrapping the provided {@link T} value.
	 *
	 * @param value the value to wrap
	 * @param <T>   The type of the wrapped value
	 * @return a new {@link Filled} instance
	 */
	public static <T> Lazy.Filled<T> filled(T value) {
		return new Filled<>(value);
	}

	/**
	 * Creates a new {@link Lazy} containing the current value mapped using the provided {@link Function}.
	 *
	 * <p>
	 * <i><b>This does not initialize the contained value!</b></i>
	 *
	 * @param mapper the function to apply to the contained value
	 * @param <U>    The new contained type
	 * @return a new {@link Lazy} instance, that may or may not be initialized that contains the mapped value
	 */
	public abstract <U> Lazy<U> map(Function<T, ? extends U> mapper);

	/**
	 * If we contain a value, we instantly return the transformed version, using the {@link Function} provided.
	 * Otherwise, we return a new {@link Lazy} containing the value of the result of the mapper {@link  Function}.
	 *
	 * <p>
	 * <i><b>This does not initialize the contained value!</b></i>
	 *
	 * @param mapper the {@link Function} to use
	 * @param <U>    The new contained type
	 * @return the mapped {@link Lazy} value
	 */
	public abstract <U> Lazy<U> flatMap(Function<T, ? extends Lazy<U>> mapper);

	/**
	 * Initializes the current {@link Lazy} and returns the result of applying the provided {@link Function} to the value.
	 *
	 * <p>
	 * <i><b>This initializes the contained value.</b></i>
	 *
	 * @param function the {@link Function} to use
	 * @param <M>      The return type
	 * @return the {@link M} value produced from the application of the provided {@link Function}}
	 */
	public <M> M mapUnwrap(Function<T, ? extends M> function) {
		return function.apply(this.unwrap());
	}

	/**
	 * Whether the current {@link Lazy} has initialized its value.
	 *
	 * @return {@code true} if the current value is initialized, or {@code false} otherwise
	 */
	public abstract boolean isFilled();

	/**
	 * Applies the provided {@link Consumer} on the current value, if it is initialized.
	 *
	 * @param action the {@link Consumer} to apply the value to
	 * @return this instance(for chaining)
	 */
	public abstract Lazy<T> ifFilled(Consumer<? super T> action);

	/**
	 * Runs the provided {@link Runnable}, if our value is not initialized.
	 *
	 * @param action the {@link Runnable} to run
	 * @return this instance(for chaining)
	 */
	public abstract Lazy<T> ifEmpty(Runnable action);

	/**
	 * Whether the current instance has an initialized value.
	 *
	 * @return {@code true} if the current value isn't initialized, or {@code false} otherwise
	 */
	public abstract boolean isEmpty();

	/**
	 * Initializes the current value and applies it to the provided {@link Consumer}.
	 *
	 * <p>
	 * <i><b>This initializes the contained value.</b></i>
	 *
	 * @param action the {@link Consumer} to accept the current value
	 * @return this instance(for chaining)
	 */
	public Lazy<T> compute(Consumer<? super T> action) {
		action.accept(this.get());
		return this;
	}

	/**
	 * @see Lazy#get
	 */
	public T unwrap() {
		return this.get();
	}

	/**
	 * Returns the contained value if it is initialized, otherwise the provided {@link T} instance.
	 *
	 * <p>
	 * <i><b>This does not initialize the contained value.</b></i>
	 *
	 * @param defaultValue the default {@link T} instance to return
	 * @return the contained value or the provided {@link T}
	 */
	public abstract T unwrapOr(T defaultValue);

	/**
	 * Works like {@link Lazy#unwrapOr} except the default {@link T} value is provided lazily.
	 *
	 * @param defaultSupplier a provider of a {@link T} instance
	 * @return either the contained value or the result of {@link Supplier#get} when called on the provided {@link Supplier}
	 */
	public abstract T unwrapOrGet(Supplier<? extends T> defaultSupplier);

	/**
	 * A {@link Lazy} with an already initialized value.
	 *
	 * <p>
	 * This class can either be created using the {@link Lazy#filled} method or by mapping a {@link Lazy} with an initialized value.
	 * A {@link Lazy} cannot be converted by itself into a {@link Filled}.
	 *
	 * @param <T> the contained type
	 */
	public static final class Filled<T> extends Lazy<T> {
		private final T value;

		/**
		 * @param value the contained value
		 */
		private Filled(T value) {
			this.value = value;
		}

		/**
		 * @see Lazy#get
		 */
		@Override
		public T get() {
			return this.value;
		}

		/**
		 * @see Lazy#map
		 */
		@Override
		public <U> Lazy<U> map(Function<T, ? extends U> mapper) {
			return filled(mapper.apply(this.value));
		}

		/**
		 * @see Lazy#flatMap
		 */
		@Override
		public <U> Lazy<U> flatMap(Function<T, ? extends Lazy<U>> mapper) {
			return mapper.apply(this.get());
		}

		/**
		 * @see Lazy#isFilled
		 */
		@Override
		public boolean isFilled() {
			return true;
		}

		/**
		 * @see Lazy#ifFilled
		 */
		@Override
		public Lazy<T> ifFilled(Consumer<? super T> action) {
			action.accept(this.get());
			return this;
		}

		/**
		 * @see Lazy#ifEmpty
		 */
		@Override
		public Lazy<T> ifEmpty(Runnable action) {
			return this;
		}

		/**
		 * @see Lazy#isEmpty
		 */
		@Override
		public boolean isEmpty() {
			return false;
		}

		/**
		 * @see Lazy#unwrapOr
		 */
		@Override
		public T unwrapOr(T defaultValue) {
			return this.value;
		}

		/**
		 * @see Lazy#unwrapOrGet
		 */
		@Override
		public T unwrapOrGet(Supplier<? extends T> defaultSupplier) {
			return this.value;
		}
	}

	/**
	 * A {@link Lazy} with either an initialized value or not.
	 *
	 * @param <T> the type of object this {@link Lazy} contains
	 */
	public static final class OfSupplier<T> extends Lazy<T> {
		private Supplier<? extends T> supplier;
		// There is no overhead caused by using a Maybe here since Maybe has statically evaluated returns for Nothing instances.
		private Maybe<T> value;

		/**
		 * @param supplier the {@link Supplier} used to initialize the value
		 */
		private OfSupplier(Supplier<? extends T> supplier) {
			this.supplier = supplier;
			this.value = Maybe.nothing();
		}

		/**
		 * @see Lazy#get
		 */
		@Override
		public T get() {
			if (this.value.isNothing()) {
				this.value = Maybe.just(this.supplier.get());
				this.supplier = null; // maybe this shouldn't happen, but I am keeping it for now
			}

			return this.value.unwrap();
		}

		/**
		 * @see Lazy#map
		 */
		@Override
		public <U> Lazy<U> map(Function<T, ? extends U> mapper) {
			return this.value.isJust() ? filled(mapper.apply(this.get())) : of(() -> mapper.apply(this.supplier.get()));
		}

		/**
		 * @see Lazy#flatMap
		 */
		@Override
		public <U> Lazy<U> flatMap(Function<T, ? extends Lazy<U>> mapper) {
			return this.value.isJust() ? mapper.apply(this.get()) : of(() -> mapper.apply(this.get()).get());
		}

		/**
		 * @see Lazy#isFilled
		 */
		@Override
		public boolean isFilled() {
			return this.value.isJust();
		}

		/**
		 * @see Lazy#ifFilled
		 */
		@Override
		public Lazy<T> ifFilled(Consumer<? super T> action) {
			this.value.ifJust(action);
			return this;
		}

		/**
		 * @see Lazy#ifEmpty
		 */
		@Override
		public Lazy<T> ifEmpty(Runnable action) {
			this.value.ifNothing(action);
			return this;
		}

		/**
		 * @see Lazy#isEmpty
		 */
		@Override
		public boolean isEmpty() {
			return this.value.isNothing();
		}

		/**
		 * @see Lazy#unwrapOr
		 */
		@Override
		public T unwrapOr(T defaultValue) {
			return this.value.unwrapOr(defaultValue);
		}

		/**
		 * @see Lazy#unwrapOrGet
		 */
		@Override
		public T unwrapOrGet(Supplier<? extends T> defaultSupplier) {
			return this.value.unwrapOrGet(defaultSupplier);
		}

		public Lazy.Filled<T> toFilled() {
			return filled(this.get());
		}
	}
}
