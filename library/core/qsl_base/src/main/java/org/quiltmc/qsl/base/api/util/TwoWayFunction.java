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
