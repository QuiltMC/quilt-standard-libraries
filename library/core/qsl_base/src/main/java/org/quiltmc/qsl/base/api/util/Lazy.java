package org.quiltmc.qsl.base.api.util;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Represents a value that is only initialized once it's needed.<br/>
 * Similar to {@link com.google.common.base.Suppliers#memoize}.
 *
 * @param <T> The type of the value this {@link Lazy} represents.
 */
public sealed abstract class Lazy<T> implements Supplier<T> permits Lazy.Filled, Lazy.OfSupplier {
	/**
	 * Wraps the provided {@link Supplier} into a {@link OfSupplier} instance.
	 * @param supplier The {@link Supplier} to a value.
	 * @return A new {@link OfSupplier} instance.
	 * @param <T> The type of the wrapped value.
	 */
    public static <T> Lazy<T> of(Supplier<? extends T> supplier) {
        return new OfSupplier<>(supplier);
    }



	/**
	 * Returns an {@link Filled} instance wrapping the provided {@link T} value.
	 *
	 * @param value The value to wrap.
	 * @return A new {@link Filled} instance.
	 * @param <T> The type of the wrapped value.
	 */
    public static <T> Lazy<T> filled(T value) {
        return new Filled<>(value);
    }

	/**
	 * Creates a new {@link Lazy} containing the current value mapped using the provided {@link Function}.<br/>
	 * <i><b>This does not initialize the contained value!</b></i>
	 *
	 * @param mapper
	 * @return
	 * @param <U> The new contained type.
	 */
    public abstract <U> Lazy<U> map(Function<T, ? extends U> mapper);

	/**
	 * If we contain a value, we instantly return the transformed version, using the {@link Function} provided.<br/>
	 * Otherwise, we return a new {@link Lazy} containing the value of the result of the mapper {@link  Function}.<br/>
	 * <i><b>This does not initialize the contained value!</b></i>
	 *
	 * @param mapper The {@link Function} to use.
	 * @return The mapped {@link Lazy} value.
	 * @param <U> The new contained type.
	 */
    public abstract <U> Lazy<U> flatMap(Function<T, ? extends Lazy<U>> mapper);

	/**
	 * Initializes the current {@link Lazy} and returns the result of applying the provided {@link Function} to the value.<br/>
	 * <i><b>This initializes the contained value.</b></i>
	 *
	 * @param function The {@link Function} to use.
	 * @return The {@link M} value produced from the application of the provided {@link Function}}.
	 * @param <M> The return type.
	 */
    public <M> M mapUnwrap(Function<T, ? extends M> function) {
        return function.apply(this.unwrap());
    }

	/**
	 * Whether the current {@link Lazy} has initialized its value.
	 * @return Whether the current {@link Lazy} contains an initialized value.
	 */
    public abstract boolean isFilled();

	/**
	 * Applies the provided {@link Consumer} on the current value, if it is initialized.
	 *
	 * @param action The {@link Consumer} to apply the value to.
	 * @return This instance(for chaining).
	 */
    public abstract Lazy<T> ifFilled(Consumer<? super T> action);

	/**
	 * Runs the provided {@link Runnable}, if our value is not initialized.<br/>
	 *
	 * @param action The {@link Runnable} to run.
	 * @return This instance(for chaining).
	 */
    public abstract Lazy<T> ifEmpty(Runnable action);

	/**
	 * Whether the current instance has an initialized value.
	 *
	 * @return Whether there is a value in this {@link Lazy} that is initialized.
	 */
    public abstract boolean isEmpty();

	/**
	 * Initializes the current value and applies it to the provided {@link Consumer}.<br/>
	 * <i><b>This initializes the contained value.</b></i>
	 *
	 * @param action The {@link Consumer} to accept the current value.
	 * @return This instance(for chaining).
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
	 * <i><b>This does not initialize the contained value.</b></i>
	 *
	 * @param defaultValue The default {@link T} instance to return.
	 * @return The contained value or the provided {@link T}.
	 */
	public abstract T unwrapOr(T defaultValue);

	/**
	 * Works like {@link Lazy#unwrapOr} except the default {@link T} value is provided lazily.
	 *
	 * @param defaultSupplier A provider of a {@link T} instance.
	 * @return Either the contained value or the result of {@link Supplier#get} when called on the provided {@link Supplier}.
	 */
	public abstract T unwrapOrGet(Supplier<? extends T> defaultSupplier);

	/**
	 * A {@link Lazy} with an already initialized value.<br/>
	 *
	 * This class can either be created using the {@link Lazy#filled} method or by mapping a {@link Lazy} with an initialized value.<br/>
	 * A {@link Lazy} cannot be converted by itself into a {@link Filled}.
	 * @param <T> The contained type.
	 */
    public static final class Filled<T> extends Lazy<T> {
        private final T value;

		/**
		 * @param value The contained value.
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
	 * @param <T>
	 */
    public static final class OfSupplier<T> extends Lazy<T> {
        private Supplier<? extends T> supplier;
		// There is no overhead to using a Maybe here since Maybe has statically evaluated returns for Nothing instances..
        private Maybe<T> value;

		/**
		 * @param supplier The {@link Supplier} used to initialize the value.
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
                this.supplier = null;
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
    }
}