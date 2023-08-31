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

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.testing.impl.game.QuiltGameTestImpl;

/**
 * Represents the registration context of modded tests.
 *
 * @param mod the mod for which the tests are registered
 */
public record TestRegistrationContext(ModContainer mod) {
	/**
	 * Registers an additional test class.
	 *
	 * @param testClass the test class to register
	 * @see #register(Class[])
	 */
	public void register(Class<?> testClass) {
		if (testClass.isAssignableFrom(QuiltGameTest.class)) {
			Constructor<?> constructor;

			try {
				constructor = testClass.getConstructor();
			} catch (NoSuchMethodException e) {
				throw new RuntimeException("Test class (%s) provided by (%s) must have a public default or no args constructor"
						.formatted(testClass.getSimpleName(), QuiltGameTestImpl.getDataForTestClass(testClass).namespace())
				);
			}

			QuiltGameTest testObject;

			try {
				testObject = (QuiltGameTest) constructor.newInstance();
			} catch (InvocationTargetException | InstantiationException | IllegalAccessException e) {
				throw new RuntimeException("Failed to create instance of test class (%s)".formatted(testClass.getCanonicalName()), e);
			}

			QuiltGameTestImpl.registerTestClass(this.mod, testClass, testObject);
		} else {
			QuiltGameTestImpl.registerTestClass(this.mod, testClass, null);
		}
	}

	/**
	 * Registers additional test classes.
	 *
	 * @param testClasses the test classes to register
	 * @see #register(Class)
	 */
	public void register(Class<?>... testClasses) {
		for (var testClass : testClasses) {
			this.register(testClass);
		}
	}
}
