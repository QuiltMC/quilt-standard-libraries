package org.quiltmc.qsl.registry.dict.api;

import org.jetbrains.annotations.NotNull;

/**
 * A function that computes a value for an entry that doesn't have one.
 *
 * @param <R> type of registry entry
 * @param <V> type of value
 */
@FunctionalInterface
public interface ComputeFunction<R, V> {
	/**
	 * Computes a value for an entry that doesn't already have one.
	 *
	 * @param entry entry to compute for
	 * @return computed value
	 * @throws ComputeFailedException if computation fails for some reason.
	 */
	@NotNull V computeFor(R entry) throws ComputeFailedException;

	/**
	 * Thrown by {@link ComputeFunction#computeFor(Object)}, should computation fail.
	 */
	class ComputeFailedException extends Exception {
		public ComputeFailedException(String message) {
			super(message);
		}

		public ComputeFailedException(String message, Throwable cause) {
			super(message, cause);
		}
	}
}
