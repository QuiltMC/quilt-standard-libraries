/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.game_test.mixin;

import java.lang.reflect.Method;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import net.minecraft.test.TestFunction;
import net.minecraft.test.TestFunctions;

import org.quiltmc.qsl.game_test.impl.QuiltGameTestImpl;

@Mixin(TestFunctions.class)
public class TestFunctionsMixin {
	/**
	 * Gets the test function from the given method.
	 *
	 * @param method the method that executes the test
	 * @return the test function
	 * @reason Replace the default test function creation with a more mod-friendly approach.
	 * @author QuiltMC, FabricMC
	 */
	@Overwrite
	private static TestFunction getTestFunction(Method method) {
		return QuiltGameTestImpl.getTestFunction(method);
	}
}
