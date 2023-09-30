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

/**
 * This interface can be optionally implemented on your test class.
 */
public interface QuiltGameTest {
	/**
	 * Use in {@link net.minecraft.test.GameTest} structureName to use an empty 8x8 structure for the test.
	 */
	String EMPTY_STRUCTURE = "quilt:empty";

	/**
	 * Represents the key of the game tests entrypoint to use in the {@code quilt.mod.json} file, whose value is {@value}.
	 */
	String ENTRYPOINT_KEY = "quilt:game_test";

	/**
	 * Override this method to implement custom logic to invoke the test method.
	 * <p>
	 * This can be used to run code before or after each test.
	 * You can also pass in custom objects into the test method if desired.
	 * The structure will have been placed in the world before this method is invoked.
	 *
	 * @param context the vanilla test context
	 * @param method  the test method to invoke
	 */
	default void invokeTestMethod(QuiltTestContext context, TestMethod method) {
		method.invoke(this, context);
	}

	/**
	 * Override this method to register additional tests or conditional tests.
	 *
	 * @param context the test registration context
	 */
	default void registerTests(TestRegistrationContext context) {}
}
