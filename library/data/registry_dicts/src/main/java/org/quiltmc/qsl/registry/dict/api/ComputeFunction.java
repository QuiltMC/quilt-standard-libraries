/*
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
