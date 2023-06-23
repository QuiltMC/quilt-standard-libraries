/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.command.api;

import com.mojang.brigadier.arguments.ArgumentType;

/**
 * A function that transforms an argument of a custom type into one understandable by a vanilla client.
 *
 * @param <T> the original argument type
 */
@FunctionalInterface
public interface ArgumentTypeFallbackProvider<T extends ArgumentType<?>> {
	/**
	 * Creates a fallback argument understandable by the vanilla client based on the specified argument.
	 *
	 * @param originalArg the original argument
	 * @return the new vanilla-compatible argument
	 */
	ArgumentType<?> createVanillaFallback(T originalArg);
}
