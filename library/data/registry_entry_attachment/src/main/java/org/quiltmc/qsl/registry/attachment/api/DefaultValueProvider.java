/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.registry.attachment.api;

import org.jetbrains.annotations.NotNull;

/**
 * A function that computes a default value for a registry entry that doesn't have one defined explicitly.
 *
 * @param <R> type of registry entry
 * @param <V> type of value
 */
@FunctionalInterface
public interface DefaultValueProvider<R, V> {
	/**
	 * Computes a value for an entry that doesn't already have one.
	 *
	 * @param entry entry to compute for
	 * @return computation result
	 */
	@NotNull Result<V> computeDefaultValue(R entry);

	/**
	 * Represents the result of computing a default value.
	 * <p>
	 * Can either have a value or an error string.
	 *
	 * @param <V> type of value
	 */
	final class Result<V> {
		private final boolean hasFailed;
		private final V value;
		private final String error;

		private Result(boolean hasFailed, V value, String error) {
			this.hasFailed = hasFailed;
			this.value = value;
			this.error = error;
		}

		/**
		 * Creates a successful {@code Result} with the specified value.
		 *
		 * @param value value
		 * @param <V>   type of value
		 * @return successful result
		 */
		public static <V> Result<V> of(V value) {
			return new Result<>(false, value, null);
		}

		/**
		 * Creates a failed {@code Result} with the specified error string.
		 *
		 * @param error error string
		 * @param <V>   type of value
		 * @return failed result
		 */
		public static <V> Result<V> ofFailure(String error) {
			return new Result<>(true, null, error);
		}

		/**
		 * {@return {@code true} if this result represents a failed computation, {@code false} otherwise}
		 */
		public boolean hasFailed() {
			return this.hasFailed;
		}

		/**
		 * {@return the value of this result}
		 */
		public V get() {
			if (this.hasFailed) {
				throw new IllegalStateException("Result is a failure!");
			}

			return this.value;
		}

		/**
		 * {@return the error string of this result}
		 */
		public String error() {
			if (!this.hasFailed) {
				throw new IllegalStateException("Result does not have an error!");
			}

			return this.error;
		}
	}
}
