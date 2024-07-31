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

package org.quiltmc.qsl.screen.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.Keyboard;
import net.minecraft.client.gui.screen.Screen;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.util.TriState;
import org.quiltmc.qsl.screen.api.client.ScreenKeyboardEvents;

@ClientOnly
@Mixin(Keyboard.class)
abstract class KeyboardMixin {
	// lambda in Screen.wrapScreenError in Keyboard.onKey
	@WrapOperation(method = "method_1454", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;keyPressed(III)Z"))
	private static boolean onKeyPressed(Screen screen, int key, int scancode, int modifiers, Operation<Boolean> original) {
		if (ScreenKeyboardEvents.ALLOW_KEY_PRESS.invoker().allowKeyPress(screen, key, scancode, modifiers) == TriState.FALSE) {
			return true;
		}

		ScreenKeyboardEvents.BEFORE_KEY_PRESS.invoker().beforeKeyPress(screen, key, scancode, modifiers);
		boolean result = original.call(screen, key, scancode, modifiers);
		ScreenKeyboardEvents.AFTER_KEY_PRESS.invoker().afterKeyPress(screen, key, scancode, modifiers);

		return result;
	}

	// lambda in Screen.wrapScreenError in Keyboard.onKey
	@WrapOperation(method = "method_1454", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;keyReleased(III)Z"))
	private static boolean onKeyReleased(Screen screen, int key, int scancode, int modifiers, Operation<Boolean> original) {
		if (ScreenKeyboardEvents.ALLOW_KEY_RELEASE.invoker().allowKeyRelease(screen, key, scancode, modifiers) == TriState.FALSE) {
			return true;
		}

		ScreenKeyboardEvents.BEFORE_KEY_RELEASE.invoker().beforeKeyRelease(screen, key, scancode, modifiers);
		boolean result = original.call(screen, key, scancode, modifiers);
		ScreenKeyboardEvents.AFTER_KEY_RELEASE.invoker().afterKeyRelease(screen, key, scancode, modifiers);

		return result;
	}
}
