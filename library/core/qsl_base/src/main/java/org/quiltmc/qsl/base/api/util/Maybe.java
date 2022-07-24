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

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

/**
 * Represents a monad that can either contain a value or no value.
 *
 * <p>
 * Similar to the {@link Optional} class but with some performance improvements
 * and quality of life changes.
 *
 * <p>
 * Can either be {@link Just} or {@link Nothing}.
 *
 * <p>
 * {@link Just} instances contain a value of type {@link T}.
 *
 * <p>
 * {@link Nothing} instances all contain no data.
 *
 * @param <T> the type of the wrapped object
 * @author 0xJoeMama
 */
public abstract sealed class Maybe<T> permits Maybe.Just, Maybe.Nothing {
	/**
	 * Similar function to {@link Optional#ofNullable} function.
	 * Wraps a {@link T} value into an {@link Maybe}.
	 *
	 * @param value a {@link Nullable} value that will be wrapped into a {@link Maybe}
	 * @param <T>   the type of object the {@link Maybe} instance will hold
	 * @return an instance of {@link Just} if the provided value is not <code>null</code>,
	 * otherwise a {@link Nothing} instance
	 * @see Optional#ofNullable
	 */
	public static <T> Maybe<T> wrap(@Nullable T value) {
		return value != null ? just(value) : nothing();
	}

	/**
	 * Similar function to {@link Optional#of}.
	 * Wraps the provided value into a {@link Just} instance.
	 *
	 * @param value a {@linkplain org.jetbrains.annotations.NotNull non-null} value that is to be wrapped.
	 * @param <T>   the type of object the {@link Maybe} instance will hold
	 * @return a {@link Just} instance containing the provided value
	 * @see Optional#of
	 */
	public static <T> Maybe.Just<T> just(T value) {
		return new Just<>(value);
	}

	/**
	 * Similar function to {@link Optional#empty()}.
	 *
	 * @param <T> the type the {@link Nothing} instance will take the form of
	 * @return a {@link Nothing} instance after casting it to the proper type
	 * @see Optional#empty()
	 */
	@SuppressWarnings("unchecked")
	public static <T> Maybe.Nothing<T> nothing() {
		return (Nothing<T>) Nothing.INSTANCE;
	}

	/**
	 * Converts an {@link Optional} into a {@link Maybe}.<br/>
	 * Exists for the sake of inter-operability.
	 *
	 * @param opt the {@link Optional} whose value will be wrapped in a {@link Maybe}
	 * @param <T> the type of the value, which the {@link Maybe} instance will contain
	 * @return a {@link Maybe} instance containing the value contained in the provided {@link Optional}
	 */
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	// Obviously since we need to convert, the parameter is Optional.
	public static <T> Maybe<T> fromOptional(Optional<T> opt) {
		return opt.map(Maybe::wrap).orElseGet(Maybe::nothing);
	}

	/**
	 * Maps the internal value of this {@link Maybe} into a new {@link Maybe} with a different value<br/>
	 * using the provided {@link Function}.
	 *
	 * @param transformer a {@link T} -> {@link U} function used in the mapping process
	 * @param <U>         the type of the value after the mapping operation
	 * @return a new {@link Just} instance containing the mapped value if the current instance is a {@link Just},
	 * otherwise {@link Nothing}
	 * @see Optional#map
	 */
	public abstract <U> Maybe<U> map(Function<? super T, ? extends U> transformer);

	/**
	 * Filters the current instance, based on whether it follows the provided {@link Predicate}.
	 *
	 * @param predicate a {@link Predicate} instance which <i>may</i> use the current value
	 * @return the current instance if {@link Predicate#test} returns true, otherwise {@link Nothing}
	 * @see Optional#filter
	 */
	public abstract Maybe<T> filter(Predicate<T> predicate);

	/**
	 * Filters the current instance based on whether mapping it with the provided {@link Function} provides
	 * {@link Nothing} or {@link Just}.
	 * Can be used as {@link Optional#flatMap} or as the <code>bind</code> operation of this monad.
	 * The table below explains in detail.
	 *
	 * @param transform a {@link Function} returning a {@link Maybe} instance instead of directly returning the
	 *                  mapping value
	 * @param <U>       the value the returned {@link Maybe} will contain
	 * @return <table>
	 * <tr>
	 * <th>This</th>
	 * <th>Function Value</th>
	 * <th>Return Value</th>
	 * </tr>
	 * <tr>
	 * <td>{@link Just}</td>
	 * <td>{@link Nothing}</td>
	 * <td>{@link Nothing}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link Nothing}</td>
	 * <td>Skipped</td>
	 * <td>{@link Nothing}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link Just} A</td>
	 * <td>{@link Just} B</td>
	 * <td>{@link Just} B</td>
	 * </tr>
	 * </table>
	 * @see Optional#flatMap
	 */
	public abstract <U> Maybe<U> filterMap(Function<? super T, ? extends Maybe<U>> transform);

	/**
	 * Whether the current instance is {@link Nothing} or not.
	 *
	 * @return {@code true} if this is a {@link Nothing}, or {@code false} otherwise
	 * @see Optional#isEmpty()
	 */
	public abstract boolean isNothing();

	/**
	 * Whether the current instance is {@link Just} or not.
	 *
	 * @return {@code true} if this is a {@link Just}, or {@code false} otherwise
	 * @see Optional#isPresent()
	 */
	public abstract boolean isJust();

	/**
	 * Applies the provided {@link Consumer} to the current value, if the current instance is {@link Just}.
	 *
	 * @param action a {@link Consumer} that <i>may</i> use the current value
	 * @return this instance(for chaining)
	 * @see Optional#ifPresent
	 */
	public abstract Maybe<T> ifJust(Consumer<? super T> action);

	/**
	 * Runs the provided {@link Runnable} if the current instance is {@link Nothing}.
	 *
	 * @param runnable the action to run
	 * @return this instance(for chaining)
	 */
	public abstract Maybe<T> ifNothing(Runnable runnable);

	/**
	 * Performs an operation similar to the boolean <code>or</code> operation on the current instance, using the one
	 * provided.
	 * The table below describes the different return values.
	 *
	 * @param supplier a {@link Supplier} of a {@link Maybe}, be it {@link Just} or {@link Nothing}
	 * @return <table>
	 * <tr>
	 * <th>This</th>
	 * <th>Supplier</th>
	 * <th>Return Value</th>
	 * </tr>
	 * <tr>
	 * <td>{@link Just}</td>
	 * <td>Skipped</td>
	 * <td>This instance</td>
	 * </tr>
	 * <tr>
	 * <td>{@link Nothing}</td>
	 * <td>{@link Just}</td>
	 * <td>The created {@link Just}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link Nothing}</td>
	 * <td>{@link Nothing}</td>
	 * <td>{@link Nothing}</td>
	 * </tr>
	 * </table>
	 * @see Optional#or
	 */
	public abstract Maybe<T> or(Supplier<? extends Maybe<T>> supplier);

	/**
	 * Performs an operation similar to the boolean <code>and</code> operation on the current instance using the one
	 * provided.
	 * The table below describes the different return values.
	 *
	 * @param other a {@link Supplier} of a {@link Maybe} instance, be it {@link Nothing} or {@link Just}
	 * @return <table>
	 * <tr>
	 * <th>This</th>
	 * <th>Supplier</th>
	 * <th>Return Value</th>
	 * </tr>
	 * <tr>
	 * <td>{@link Nothing}</td>
	 * <td>Skipped</td>
	 * <td>{@link Nothing}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link Just}</td>
	 * <td>{@link Nothing}</td>
	 * <td>{@link Nothing}</td>
	 * </tr>
	 * <tr>
	 * <td>{@link Just} A</td>
	 * <td>{@link Just} B</td>
	 * <td>{@link Just} A</td>
	 * </tr>
	 * </table>
	 */
	public abstract Maybe<T> and(Supplier<? extends Maybe<T>> other);

	/**
	 * Returns the {@link T} value container in the current {@link Maybe} instance.
	 *
	 * @return the {@link T} value that was wrapped
	 * @throws IllegalStateException if the current instance is {@link Nothing}.
	 * @see Optional#orElseThrow()
	 */
	public abstract T unwrap() throws IllegalStateException;

	/**
	 * Works like {@link Maybe#unwrap()} except that it doesn't throw if the current instance if {@link Nothing}.
	 * In that case it returns the provided {@link T} value.
	 *
	 * @param defaultValue the value to get if the current instance if {@link Nothing}
	 * @return the current {@link T} value if the current instance is {@link Just}, otherwise the provided {@link T}
	 * value
	 * @see Optional#orElse
	 */
	public abstract T unwrapOr(T defaultValue);

	/**
	 * Works like {@link Maybe#unwrapOr} except that it lazily evaluates the return value using the provided
	 * {@link Supplier}.
	 *
	 * @param supplier a {@link Supplier} that will create the {@link T} value to be returns if the current instance
	 *                 is {@link Nothing}
	 * @return the current {@link T} value if the current instance if {@link Just},
	 * otherwise the value produces by calling {@link Supplier#get()} on the provided {@link Supplier}
	 * @see Optional#orElseGet
	 */
	public abstract T unwrapOrGet(Supplier<? extends T> supplier);

	/**
	 * Works like {@link Maybe#unwrap} except it throws the provided {@link Throwable} and not the default one.
	 *
	 * @param throwableSupplier the {@link Throwable} to throw
	 * @return the contained value, if it exists
	 * @throws E a customizable throwable
	 */
	public abstract <E extends Throwable> T unwrapOrThrow(Supplier<? extends E> throwableSupplier) throws E;

	/**
	 * Turns the current {@link Maybe} instance into an {@link Optional}, for inter-operability.
	 *
	 * @return {@link Optional#empty()} if the current instance is {@link Nothing},
	 * otherwise an {@link Optional} containing the current value
	 */
	public abstract Optional<T> toOptional();

	/**
	 * Attempts to perform an unchecked, unsafe cast on the current instance.
	 *
	 * @param <U> the target type
	 * @return the cast instance
	 */
	@SuppressWarnings("unchecked")
	public <U> Maybe<U> castUnchecked() {
		return (Maybe<U>) this;
	}

	/**
	 * The {@link Maybe} state representing the absence of a value.
	 *
	 * @param <T> in this case this type parameter is unused
	 */
	public static final class Nothing<T> extends Maybe<T> {
		/**
		 * A singleton instance is used for Nothing since it doesn't contain data and can therefore be cast to any
		 * <code>Maybe</code> type.
		 */
		private static final Nothing<?> INSTANCE = new Nothing<>();

		private Nothing() { }

		/**
		 * @see Maybe#map
		 */
		@Override
		public <U> Maybe<U> map(Function<? super T, ? extends U> transformer) {
			return nothing();
		}

		/**
		 * @see Maybe#filter
		 */
		@Override
		public Maybe<T> filter(Predicate<T> predicate) {
			return this;
		}

		/**
		 * @see Maybe#filterMap
		 */
		@Override
		public <U> Maybe<U> filterMap(Function<? super T, ? extends Maybe<U>> transform) {
			return nothing();
		}

		/**
		 * @see Maybe#isNothing
		 */
		@Override
		public boolean isNothing() {
			return true;
		}

		/**
		 * @see Maybe#isJust
		 */
		@Override
		public boolean isJust() {
			return false;
		}

		/**
		 * @see Maybe#ifJust
		 */
		@Override
		public Maybe<T> ifJust(Consumer<? super T> action) {
			return this;
		}

		/**
		 * @see Maybe#ifNothing
		 */
		@Override
		public Maybe<T> ifNothing(Runnable runnable) {
			runnable.run();
			return this;
		}

		/**
		 * @see Maybe#or
		 */
		@Override
		public Maybe<T> or(Supplier<? extends Maybe<T>> supplier) {
			return supplier.get();
		}

		/**
		 * @see Maybe#and
		 */
		@Override
		public Maybe<T> and(Supplier<? extends Maybe<T>> other) {
			return this;
		}

		/**
		 * @see Maybe#unwrap
		 */
		@Override
		public T unwrap() throws IllegalStateException {
			throw new IllegalStateException("Attempted to unwrap a None!");
		}

		/**
		 * @see Maybe#unwrapOr
		 */
		@Override
		public T unwrapOr(T defaultValue) {
			return defaultValue;
		}

		/**
		 * @see Maybe#unwrapOrGet
		 */
		@Override
		public T unwrapOrGet(Supplier<? extends T> supplier) {
			return supplier.get();
		}

		/**
		 * @see Maybe#unwrapOrThrow
		 */
		@Override
		public <E extends Throwable> T unwrapOrThrow(Supplier<? extends E> throwableSupplier) throws E {
			throw throwableSupplier.get();
		}

		/**
		 * @see Maybe#toOptional
		 */
		@Override
		public Optional<T> toOptional() {
			return Optional.empty();
		}
	}

	/**
	 * The {@link Maybe} state representing the existence of a {@link T} value.
	 *
	 * @param <T> the type of the contained value
	 */
	public static final class Just<T> extends Maybe<T> {
		private final T value;

		private Just(T value) {
			this.value = value;
		}

		/**
		 * @see Maybe#map
		 */
		@Override
		public <U> Maybe<U> map(Function<? super T, ? extends U> transformer) {
			return just(transformer.apply(this.unwrap()));
		}

		/**
		 * @see Maybe#filter
		 */
		@Override
		public Maybe<T> filter(Predicate<T> predicate) {
			return predicate.test(this.unwrap()) ? this : nothing();
		}

		/**
		 * @see Maybe#filterMap
		 */
		@Override
		public <U> Maybe<U> filterMap(Function<? super T, ? extends Maybe<U>> transform) {
			return transform.apply(this.unwrap());
		}

		/**
		 * @see Maybe#isNothing
		 */
		@Override
		public boolean isNothing() {
			return false;
		}

		/**
		 * @see Maybe#isJust
		 */
		@Override
		public boolean isJust() {
			return true;
		}

		/**
		 * @see Maybe#ifJust
		 */
		@Override
		public Maybe<T> ifJust(Consumer<? super T> action) {
			action.accept(this.unwrap());
			return this;
		}

		/**
		 * @see Maybe#ifNothing
		 */
		@Override
		public Maybe<T> ifNothing(Runnable runnable) {
			return this;
		}

		/**
		 * @see Maybe#or
		 */
		@Override
		public Maybe<T> or(Supplier<? extends Maybe<T>> supplier) {
			return this;
		}

		/**
		 * @see Maybe#and
		 */
		@Override
		public Maybe<T> and(Supplier<? extends Maybe<T>> other) {
			Maybe<T> otherMaybe = other.get();
			return otherMaybe.isNothing() ? nothing() : this;
		}

		/**
		 * @see Maybe#unwrap
		 */
		@Override
		public T unwrap() {
			return this.value;
		}

		/**
		 * @see Maybe#unwrapOr
		 */
		@Override
		public T unwrapOr(T defaultValue) {
			return this.unwrap();
		}

		/**
		 * @see Maybe#unwrapOrGet
		 */
		@Override
		public T unwrapOrGet(Supplier<? extends T> supplier) {
			return this.unwrap();
		}

		/**
		 * @see Maybe#unwrapOrThrow
		 */
		@Override
		public <E extends Throwable> T unwrapOrThrow(Supplier<? extends E> throwableSupplier) {
			return this.unwrap();
		}

		/**
		 * @see Maybe#toOptional
		 */
		@Override
		public Optional<T> toOptional() {
			// Using Optional$of since our value should never be null!
			return Optional.of(this.value);
		}
	}
}
