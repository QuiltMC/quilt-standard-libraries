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

import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Represents a monad that can either contain a value or no value.<br/>
 * Similar to the {@link Optional} class but with some performance improvements
 * and quality of life changes.<br/>
 *
 * Can either be {@link Just} or {@link Nothing}.<br/>
 * {@link Just} instances contain a value of type {@link T}.<br/>
 * {@link Nothing} instances all contain no data.<br/>
 *
 * @param <T> The type of the wrapped object
 * @author 0xJoeMama
 */
public sealed abstract class Maybe<T> permits Maybe.Just, Maybe.Nothing {
	/**
	 * Similar function to {@link Optional#ofNullable} function.<br/>
	 * Wraps a {@link T} value into an {@link Maybe}.
	 *
	 * @param value A {@link Nullable} value that will be wrapped into a {@link Maybe}.
	 * @param <T>   The type of object the {@link Maybe} instance will hold.
	 * @return An instance of {@link Just} if the provided value is not <code>null</code>,<br/>
	 * otherwise a {@link Nothing} instance.
	 * @see Optional#ofNullable
	 */
	public static <T> Maybe<T> wrap(@Nullable T value) {
		return value != null ? just(value) : nothing();
	}

	/**
	 * Similar function to {@link Optional#of}.<br/>
	 * Wraps the provided value into a {@link Just} instance.
	 *
	 * @param value A {@linkplain org.jetbrains.annotations.NotNull non-null} value that is to be wrapped.
	 * @param <T>   The type of object the {@link Maybe} instance will hold.
	 * @return A {@link Just} instance containing the provided value.
	 * @see Optional#of
	 */
	public static <T> Maybe<T> just(T value) {
		return new Just<>(value);
	}

	/**
	 * Similar function to {@link Optional#empty()}.
	 *
	 * @param <T> The type the {@link Nothing} instance will take the form of.
	 * @return A {@link Nothing} instance after casting it to the proper type.
	 * @see Optional#empty()
	 */
	@SuppressWarnings("unchecked") // Nothing doesn't contain a value, so we can freely cast it!
	public static <T> Maybe<T> nothing() {
		return (Maybe<T>) Nothing.INSTANCE;
	}

	/**
	 * Converts an {@link Optional} into a {@link Maybe}.<br/>
	 * Exists for the sake of inter-operability.
	 *
	 * @param opt The {@link Optional} whose value will be wrapped in a {@link Maybe}.
	 * @param <T> The type of the value, which the {@link Maybe} instance will contain.
	 * @return A {@link Maybe} instance containing the value contained in the provided {@link Optional}.
	 */
	@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
	// Obviously since we need to convert, the parameter is Optional.
	public static <T> Maybe<T> fromOptional(Optional<T> opt) {
		return opt.map(Maybe::just).orElseGet(Maybe::nothing);
	}

	/**
	 * Maps the internal value of this {@link Maybe} into a new {@link Maybe} with a different value<br/>
	 * using the provided {@link Function}.
	 *
	 * @param transformer A {@link T} -> {@link U} function used in the mapping process.
	 * @param <U>         The type of the value after the mapping operation.
	 * @return A new {@link Just} instance containing the mapped value if the current instance is a {@link Just},
	 * otherwise {@link Nothing}.
	 * @see Optional#map
	 */
	public abstract <U> Maybe<U> map(Function<? super T, ? extends U> transformer);

	/**
	 * Filters the current instance, based on whether it follows the provided {@link Predicate}.
	 *
	 * @param predicate A {@link Predicate} instance which <i>may</i> use the current value.
	 * @return The current instance if {@link Predicate#test} returns true, otherwise {@link Nothing}.
	 * @see Optional#filter
	 */
	public abstract Maybe<T> filter(Predicate<T> predicate);

	/**
	 * Filters the current instance based on whether mapping it with the provided {@link Function} provides {@link Nothing} or {@link Just}.
	 * Can be used as {@link Optional#flatMap} or as the <code>bind</code> operation of this monad.
	 * The table below explains in detail.
	 *
	 * @param transform A {@link Function} returning a {@link Maybe} instance instead of directly returning the mapping value.
	 * @param <U>       The value the returned {@link Maybe} will contain.
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
	 * @return A boolean expressing the aforementioned fact.
	 * @see Optional#isEmpty()
	 */
	public abstract boolean isNothing();

	/**
	 * Whether the current instance is {@link Just} or not.
	 *
	 * @return A boolean expressing the aforementioned fact.
	 * @see Optional#isPresent()
	 */
	public abstract boolean isJust();

	/**
	 * Applies the provided {@link Consumer} to the current value, if the current instance is {@link Just}.
	 *
	 * @param action A {@link Consumer} that <i>may</i> use the current value.
	 * @return This instance(for chaining).
	 * @see Optional#ifPresent
	 */
	public abstract Maybe<T> ifJust(Consumer<? super T> action);

	/**
	 * Runs the provided {@link Runnable} if the current instance is {@link Nothing}.
	 *
	 * @param runnable The action to run.
	 * @return This instance(for chaining).
	 */
	public abstract Maybe<T> ifNothing(Runnable runnable);

	/**
	 * Performs an operation similar to the boolean <code>or</code> operation on the current instance, using the one provided.
	 * The table below describes the different return values.
	 *
	 * @param supplier A {@link Supplier} of a {@link Maybe}, be it {@link Just} or {@link Nothing}.
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
	 * Performs an operation similar to the boolean <code>and</code> operation on the current instance using the one provided.
	 * The table below describes the different return values.
	 *
	 * @param other A {@link Supplier} of a {@link Maybe} instance, be it {@link Nothing} or {@link Just}.
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
	 * @return The {@link T} value that was wrapped.
	 * @throws IllegalStateException if the current instance is {@link Nothing}.
	 * @see Optional#orElseThrow()
	 */
	public abstract T unwrap() throws IllegalStateException;

	/**
	 * Works like {@link Maybe#unwrap()} except that it doesn't throw if the current instance if {@link Nothing}.
	 * In that case it returns the provided {@link T} value.
	 *
	 * @param defaultValue The value to get if the current instance if {@link Nothing}.
	 * @return The current {@link T} value if the current instance is {@link Just}, otherwise the provided {@link T} value.
	 * @see Optional#orElse
	 */
	public abstract T unwrapOr(T defaultValue);

	/**
	 * Works like {@link Maybe#unwrapOr} except that it lazily evaluates the return value using the provided {@link Supplier}.
	 *
	 * @param supplier A {@link Supplier} that will create the {@link T} value to be returns if the current instance is {@link Nothing}.
	 * @return The current {@link T} value if the current instance if {@link Just},
	 * otherwise the value produces by calling {@link Supplier#get()} on the provided {@link Supplier}.
	 * @see Optional#orElseGet
	 */
	public abstract T unwrapOrGet(Supplier<? extends T> supplier);

	/**
	 * Works like {@link Maybe#unwrap} except it throws the provided {@link Throwable} and not the default one.
	 *
	 * @param throwableSupplier The {@link Throwable} to throw.
	 * @return The contained value, if it exists.
	 * @throws Throwable A customizable throwable.
	 */
	public abstract <E extends Throwable> T unwrapOrThrow(Supplier<? extends E> throwableSupplier) throws E;

	/**
	 * Turns the current {@link Maybe} instance into an {@link Optional}, for inter-operability.
	 *
	 * @return {@link Optional#empty()} if the current instance is {@link Nothing},
	 * otherwise an {@link Optional} containing the current value.
	 */
	public abstract Optional<T> toOptional();

	/**
	 * The {@link Maybe} state representing the absence of a value.
	 *
	 * @param <T> In this case this type parameter is unused.
	 */
	public static final class Nothing<T> extends Maybe<T> {
		/**
		 * A singleton instance is used for Nothing since it doesn't contain data and can therefore be cast to any <code>Maybe</code> type.
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
	 * @param <T> The type of the contained value.
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
		public <E extends Throwable> T unwrapOrThrow(Supplier<? extends E> throwableSupplier) throws E {
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
