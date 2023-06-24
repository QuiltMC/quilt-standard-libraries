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

package org.quiltmc.qsl.testing.mixin.command;

import com.mojang.brigadier.arguments.ArgumentType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.SharedConstants;
import net.minecraft.command.argument.ArgumentTypeInfo;
import net.minecraft.command.argument.ArgumentTypeInfos;
import net.minecraft.command.argument.SingletonArgumentInfo;
import net.minecraft.command.argument.TestClassArgumentType;
import net.minecraft.command.argument.TestFunctionArgumentType;
import net.minecraft.registry.Registry;

import org.quiltmc.qsl.testing.impl.game.QuiltGameTestImpl;
import org.quiltmc.qsl.testing.impl.game.command.TestNameArgumentType;

@Mixin(ArgumentTypeInfos.class)
public abstract class ArgumentTypeInfosMixin {
	@Shadow
	private static <A extends ArgumentType<?>, T extends ArgumentTypeInfo.Template<A>> ArgumentTypeInfo<A, T> register(
			Registry<ArgumentTypeInfo<?, ?>> registry, String string, Class<? extends A> clazz, ArgumentTypeInfo<A, T> argumentTypeInfo) {
		throw new IllegalStateException("Mixin injection failed.");
	}

	@Inject(method = "bootstrap", at = @At("RETURN"))
	private static void register(Registry<ArgumentTypeInfo<?, ?>> registry, CallbackInfoReturnable<ArgumentTypeInfo<?, ?>> ci) {
		// Registered by vanilla when isDevelopment is enabled.
		if (QuiltGameTestImpl.COMMAND_ENABLED) {
			if (!SharedConstants.isDevelopment) {
				register(registry, "test_argument", TestFunctionArgumentType.class,
						SingletonArgumentInfo.contextFree(TestFunctionArgumentType::testFunction)
				);
				register(registry, "test_class", TestClassArgumentType.class,
						SingletonArgumentInfo.contextFree(TestClassArgumentType::testClass)
				);
			}

			register(registry, "quilt_game_test:test_name", TestNameArgumentType.class,
					SingletonArgumentInfo.contextFree(TestNameArgumentType::new)
			);
		}
	}
}
