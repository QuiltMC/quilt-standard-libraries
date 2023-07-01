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

package org.quiltmc.qsl.testing.api.game;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import org.quiltmc.qsl.testing.impl.game.QuiltGameTestImpl;

/**
 * Represents the method to execute in a test.
 *
 * @param method the method to execute
 */
public record TestMethod(@NotNull Method method) {
	/**
	 * {@return {@code true} if the method is static, or {@code false} otherwise}
	 */
	@Contract(pure = true)
	public boolean isStatic() {
		return (this.method.getModifiers() & Modifier.STATIC) != 0;
	}

	/**
	 * {@return the declaring class of this method}
	 */
	@Contract(pure = true)
	public Class<?> getDeclaringClass() {
		return this.method.getDeclaringClass();
	}

	/**
	 * Runs the test method.
	 *
	 * @param instance the instance object given to the method, or {@code null} if the method is {@link #isStatic() static}
	 * @param params   the parameters passed to the method
	 */
	public void invoke(Object instance, Object... params) {
		try {
			this.method.invoke(instance, params);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Failed to invoke test method (%s) in (%s) because %s"
					.formatted(this.method.getName(), this.method.getDeclaringClass().getCanonicalName(), e.getMessage()),
					e
			);
		} catch (InvocationTargetException e) {
			QuiltGameTestImpl.LOGGER.error("Exception occurred when invoking test method {} in ({})",
					this.method.getName(), this.method.getDeclaringClass().getCanonicalName(), e
			);

			if (e.getCause() instanceof RuntimeException runtimeException) {
				throw runtimeException;
			} else {
				throw new RuntimeException(e.getCause());
			}
		}
	}
}
