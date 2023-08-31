/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.base.api.util;

import java.util.Objects;
import java.util.function.BooleanSupplier;

import org.jetbrains.annotations.Nullable;

/**
 * A enum that represents either {@code true}, {@code false} or a default/unset value.
 */
public enum TriState {
	/**
	 * Represents a value of {@code true}.
	 */
	TRUE,
	/**
	 * Represents a value of {@code false}.
	 */
	FALSE,
	/**
	 * Represents a default/fallback value.
	 */
	DEFAULT;

	/**
	 * Converts this TriState into a boxed boolean.
	 *
	 * @return the boolean value of the TriState. {@link #DEFAULT} will be represented by {@code null}
	 */
	public @Nullable Boolean toBoolean() {
		return switch (this) {
			case TRUE -> true;
			case FALSE -> false;
			default -> null;
		};
	}

	/**
	 * Converts this TriState into boolean. When the TriState is {@link #DEFAULT}, the boolean parameter
	 * will be returned instead.
	 *
	 * @param fallback The value to return if the TriState is {@link #DEFAULT}.
	 * @return the boolean value of the TriState. {@link #DEFAULT} will be represented by the fallback parameter
	 */
	public boolean toBooleanOrElse(boolean fallback) {
		return switch (this) {
			case TRUE -> true;
			case FALSE -> false;
			default -> fallback;
		};
	}

	/**
	 * Converts this TriState into boolean. When the TriState is {@link #DEFAULT}, the boolean parameter
	 * will be returned instead.
	 *
	 * @param fallbackSupplier The supplier to get the value to return if the TriState is {@link #DEFAULT} from.
	 * @return the boolean value of the TriState. {@link #DEFAULT} will be represented by the boolean fetched from the fallbackSupplier parameter
	 */
	public boolean toBooleanOrElseGet(BooleanSupplier fallbackSupplier) {
		Objects.requireNonNull(fallbackSupplier, "fallbackSupplier may not be null");

		return switch (this) {
			case TRUE -> true;
			case FALSE -> false;
			default -> fallbackSupplier.getAsBoolean();
		};
	}

	/**
	 * Converts the specified boolean into a TriState.
	 *
	 * @param bool the boolean to convert
	 * @return the TriState value of the boolean
	 */
	public static TriState fromBoolean(boolean bool) {
		return bool ? TRUE : FALSE;
	}

	/**
	 * Converts the specified boxed boolean into a TriState.
	 *
	 * @param bool the boxed boolean to convert
	 * @return the TriState value of the boolean
	 */
	public static TriState fromBoolean(@Nullable Boolean bool) {
		return bool == null ? DEFAULT : fromBoolean(bool);
	}

	/**
	 * {@return a parsed TriState from a system property}
	 *
	 * @param property the system property
	 */
	public static TriState fromProperty(String property) {
		String value = System.getProperty(property);

		if ("true".equalsIgnoreCase(value) || "on".equalsIgnoreCase(value)) {
			return TRUE;
		} else if ("false".equalsIgnoreCase(value) || "off".equalsIgnoreCase(value)) {
			return FALSE;
		} else {
			return DEFAULT;
		}
	}
}
