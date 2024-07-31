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

import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.util.TriState;
import org.quiltmc.qsl.screen.api.client.ScreenMouseEvents;

@ClientOnly
@Mixin(Mouse.class)
abstract class MouseMixin {
	// Synthetic lambda in Screen.wrapScreenError in Mouse.onMouseButton
	@WrapOperation(
			method = "method_1611([ZLnet/minecraft/client/gui/screen/Screen;DDI)V",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/gui/screen/Screen;mouseClicked(DDI)Z"
			)
	)
	private static boolean mouseClickedEvent(Screen instance, double mouseX, double mouseY, int button, Operation<Boolean> original) {
		if (ScreenMouseEvents.ALLOW_MOUSE_CLICK.invoker().allowMouseClick(instance, mouseX, mouseY, button) == TriState.FALSE) {
			return true;
		}

		ScreenMouseEvents.BEFORE_MOUSE_CLICK.invoker().beforeMouseClick(instance, mouseX, mouseY, button);
		boolean result = original.call(instance, mouseX, mouseY, button);
		ScreenMouseEvents.AFTER_MOUSE_CLICK.invoker().afterMouseClick(instance, mouseX, mouseY, button);

		return result;
	}

	// Synthetic lambda in Screen.wrapScreenError in Mouse.onMouseButton
	@WrapOperation(
			method = "method_1605([ZLnet/minecraft/client/gui/screen/Screen;DDI)V",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/gui/screen/Screen;mouseReleased(DDI)Z"
			)
	)
	private static boolean mouseReleasedEvent(Screen instance, double mouseX, double mouseY, int button, Operation<Boolean> original) {
		if (ScreenMouseEvents.ALLOW_MOUSE_RELEASE.invoker().allowMouseRelease(instance, mouseX, mouseY, button) == TriState.FALSE) {
			return true;
		}

		ScreenMouseEvents.BEFORE_MOUSE_RELEASE.invoker().beforeMouseRelease(instance, mouseX, mouseY, button);
		boolean result = original.call(instance, mouseX, mouseY, button);
		ScreenMouseEvents.AFTER_MOUSE_RELEASE.invoker().afterMouseRelease(instance, mouseX, mouseY, button);

		return result;
	}

	@WrapOperation(
			method = "onMouseScroll",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/gui/screen/Screen;mouseScrolled(DDDD)Z"
			)
	)
	private boolean mouseScrolledEvent(Screen instance, double mouseX, double mouseY, double scrollDistanceX, double scrollDistanceY, Operation<Boolean> original) {
		if (ScreenMouseEvents.ALLOW_MOUSE_SCROLL.invoker().allowMouseScroll(instance, mouseX, mouseY, scrollDistanceX, scrollDistanceY) == TriState.FALSE) {
			return true;
		}

		ScreenMouseEvents.BEFORE_MOUSE_SCROLL.invoker().beforeMouseScroll(instance, mouseX, mouseY, scrollDistanceX, scrollDistanceY);
		boolean result = original.call(instance, mouseX, mouseY, scrollDistanceX, scrollDistanceY);
		ScreenMouseEvents.AFTER_MOUSE_SCROLL.invoker().afterMouseScroll(instance, mouseX, mouseY, scrollDistanceX, scrollDistanceY);

		return result;
	}
}
