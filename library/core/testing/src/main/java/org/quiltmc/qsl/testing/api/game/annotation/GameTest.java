/*
 * Copyright 2025 The Quilt Project
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

package org.quiltmc.qsl.testing.api.game.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.minecraft.util.BlockRotation;

/**
 * {@code GameTest} is used to tell the test framework that the annotated method is a test.
 *
 * <p>{@code GameTest} methods must take 1 parameter of {@link net.minecraft.test.TestContext} </p>
 *
 * <b>Important note:</b> This annotation is provided only for a smooth transition to new versions,
 * in a future update it will be removed in favor of a more modern approach.
 */
@Deprecated
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface GameTest {
	/**
	 *  The number of ticks after which the test automatically fails if it has not yet completed.
	 */
	int timeout() default 100;

	boolean skyAccess() default false;

	/**
	 * The ordinal of a {@link net.minecraft.util.BlockRotation} value for the rotation of the test structure.
	 */
	BlockRotation rotation() default BlockRotation.NONE;

	/**
	 * Whether this test must succeed for the whole test sequence to succeed.
	 */
	boolean required() default true;

	/**
	 * Where this test can only be done by players.
	 */
	boolean manualOnly() default false;

	/**
	 * An {@link net.minecraft.util.Identifier} describing the location of the structure file to load for this test.
	 *
	 * <p>The actual path for the file depends on the current test framework, but usually gets resolved as
	 * {@code "<namespace>:game_test/structures/<location>.nbt"}
	 */
	String structureName() default "";

	/**
	 * The number of ticks to wait between loading the structure and starting the test.
	 */
	long startDelay() default 0L;

	/**
	 * The maximum amount of times this test may run
	 *
	 * <p>When this number is above one, the annotated test method may be called again once the previous run has
	 * completed (successfully or not), if the number of {@link #requiredSuccesses} has not been not reached.
	 */
	int maxAttempts() default 1;

	/**
	 * The minimum number of successes - out of all attempts - for this test to be considered successful.
	 */
	int requiredSuccesses() default 1;
}
