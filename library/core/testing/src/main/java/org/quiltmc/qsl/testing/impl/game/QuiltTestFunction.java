/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.testing.impl.game;

import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.test.TestContext;
import net.minecraft.test.TestFunction;
import net.minecraft.util.BlockRotation;

/**
 * Represents Quilt's extended {@link TestFunction}.
 */
@ApiStatus.Internal
public final class QuiltTestFunction extends TestFunction {
	private final Class<?> sourceClass;

	public QuiltTestFunction(String batchId, String structurePath, String structureName, BlockRotation rotation, int timeout, long startDelay, boolean required, int requiredSuccesses, int maxAttempts, Consumer<TestContext> starter, Class<?> sourceClass) {
		super(batchId, structurePath, structureName, rotation, timeout, startDelay, required, requiredSuccesses, maxAttempts, starter);
		this.sourceClass = sourceClass;
	}

	public Class<?> getSourceClass() {
		return this.sourceClass;
	}
}
