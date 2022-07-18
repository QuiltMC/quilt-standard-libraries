package org.quiltmc.qsl.base.api.util;

/**
 * Represents an object that can trasmute between 2 types of objects provided it has one of them available.
 *
 * @param <T> The first object type
 * @param <U> The second object type
 */
public interface TwoWayFunction<T, U> {
	/**
	 * Transform a {@link U} instance into a {@link T} instance.
	 * @param u The {@link U} instance to transform.
	 * @return The produced {@link T} instance
	 */
	T to(U u);

	/**
	 * Transform a {@link T} instance into a {@link U} instance.
	 * @param t The {@link T} instance to transform.
	 * @return The produced {@link T} instance.
	 */
	U from(T t);
}
