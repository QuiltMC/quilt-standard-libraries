/*
 * Copyright 2021-2022 QuiltMC
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

import java.util.function.Function;

/**
 * A set of various assertion utilities.
 */
public final class QuiltAssertions {
	private QuiltAssertions() {
		throw new UnsupportedOperationException("QuiltAssertions only contains static definitions.");
	}

	/**
	 * Ensures that the given array does not contain duplicates, otherwise throw an exception.
	 *
	 * @param items            the array of items to check
	 * @param exceptionFactory the exception factory in the case of a duplicate
	 * @param <T>              the type of items of the array
	 */
	public static <T> void ensureNoDuplicates(T[] items, Function<T, IllegalArgumentException> exceptionFactory) {
		for (int i = 0; i < items.length; ++i) {
			for (int j = i + 1; j < items.length; ++j) {
				if (items[i].equals(items[j])) {
					throw exceptionFactory.apply(items[i]);
				}
			}
		}
	}
}
