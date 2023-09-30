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

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import net.minecraft.client.gui.screen.Screen;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.util.TriState;
import org.quiltmc.qsl.screen.api.client.ScreenMouseEvents;

@ClientOnly
@Mixin(Mouse.class)
abstract class MouseMixin {
	@Shadow
	@Final
	private MinecraftClient client;
	@Unique
	private Screen quilt$currentScreen;
	@Unique
	private Double quilt$scrollDistanceX;

	// Synthetic lambda in Screen.wrapScreenError in Mouse.onMouseButton
	@Inject(
			method = "method_1611([ZLnet/minecraft/client/gui/screen/Screen;DDI)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screen/Screen;mouseClicked(DDI)Z"
			),
			cancellable = true
	)
	private static void beforeMouseClickedEvent(boolean[] resultHack, Screen screen, double mouseX, double mouseY, int button, CallbackInfo ci) {
		var thisRef = (MouseMixin) (Object) MinecraftClient.getInstance().mouse;
		// Store the screen in a variable in case someone tries to change the screen during this before event.
		// If someone changes the screen, the after event will likely have class cast exceptions or throw a NPE.
		thisRef.quilt$currentScreen = thisRef.client.currentScreen;

		if (thisRef.quilt$currentScreen == null) {
			return;
		}

		if (ScreenMouseEvents.ALLOW_MOUSE_CLICK.invoker().allowMouseClick(thisRef.quilt$currentScreen, mouseX, mouseY, button) == TriState.FALSE) {
			resultHack[0] = true; // Set this press action as handled.
			thisRef.quilt$currentScreen = null;
			ci.cancel(); // Exit the lambda
			return;
		}

		ScreenMouseEvents.BEFORE_MOUSE_CLICK.invoker().beforeMouseClick(thisRef.quilt$currentScreen, mouseX, mouseY, button);
	}

	// Synthetic lambda in Screen.wrapScreenError in Mouse.onMouseButton
	@Inject(
			method = "method_1611([ZLnet/minecraft/client/gui/screen/Screen;DDI)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screen/Screen;mouseClicked(DDI)Z",
					shift = At.Shift.AFTER
			)
	)
	private static void afterMouseClickedEvent(boolean[] resultHack, Screen screen, double mouseX, double mouseY, int button, CallbackInfo ci) {
		var thisRef = (MouseMixin) (Object) MinecraftClient.getInstance().mouse;

		if (thisRef.quilt$currentScreen == null) {
			return;
		}

		ScreenMouseEvents.AFTER_MOUSE_CLICK.invoker().afterMouseClick(thisRef.quilt$currentScreen, mouseX, mouseY, button);
		thisRef.quilt$currentScreen = null;
	}

	// Synthetic lambda in Screen.wrapScreenError in Mouse.onMouseButton
	@Inject(
			method = "method_1605([ZLnet/minecraft/client/gui/screen/Screen;DDI)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screen/Screen;mouseReleased(DDI)Z"
			),
			cancellable = true
	)
	private static void beforeMouseReleasedEvent(boolean[] resultHack, Screen screen, double mouseX, double mouseY, int button, CallbackInfo ci) {
		var thisRef = (MouseMixin) (Object) MinecraftClient.getInstance().mouse;

		// Store the screen in a variable in case someone tries to change the screen during this before event.
		// If someone changes the screen, the after event will likely have class cast exceptions or throw a NPE.
		thisRef.quilt$currentScreen = thisRef.client.currentScreen;

		if (thisRef.quilt$currentScreen == null) {
			return;
		}

		if (ScreenMouseEvents.ALLOW_MOUSE_RELEASE.invoker().allowMouseRelease(thisRef.quilt$currentScreen, mouseX, mouseY, button) == TriState.FALSE) {
			resultHack[0] = true; // Set this press action as handled.
			thisRef.quilt$currentScreen = null;
			ci.cancel(); // Exit the lambda
			return;
		}

		ScreenMouseEvents.BEFORE_MOUSE_RELEASE.invoker().beforeMouseRelease(thisRef.quilt$currentScreen, mouseX, mouseY, button);
	}

	// Synthetic lambda in Screen.wrapScreenError in Mouse.onMouseButton
	@Inject(
			method = "method_1605([ZLnet/minecraft/client/gui/screen/Screen;DDI)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screen/Screen;mouseReleased(DDI)Z",
					shift = At.Shift.AFTER
			)
	)
	private static void afterMouseReleasedEvent(boolean[] resultHack, Screen screen, double mouseX, double mouseY, int button, CallbackInfo ci) {
		var thisRef = (MouseMixin) (Object) MinecraftClient.getInstance().mouse;

		if (thisRef.quilt$currentScreen == null) {
			return;
		}

		ScreenMouseEvents.AFTER_MOUSE_RELEASE.invoker().afterMouseRelease(thisRef.quilt$currentScreen, mouseX, mouseY, button);
		thisRef.quilt$currentScreen = null;
	}

	@Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseScrolled(DDD)Z"), locals = LocalCapture.CAPTURE_FAILHARD, cancellable = true)
	private void beforeMouseScrollEvent(long window, double scrollDeltaX, double scrollDeltaY, CallbackInfo ci, double scrollDistanceY, double mouseX, double mouseY) {
		// Store the screen in a variable in case someone tries to change the screen during this before event.
		// If someone changes the screen, the after event will likely have class cast exceptions or throw a NPE.
		this.quilt$currentScreen = this.client.currentScreen;

		if (this.quilt$currentScreen == null) {
			return;
		}

		// Apply same calculations to horizontal scroll as vertical scroll amount has
		this.quilt$scrollDistanceX = this.client.options.getDiscreteMouseScroll().get()
				? Math.signum(scrollDeltaX)
				: scrollDeltaX * this.client.options.getMouseWheelSensitivity().get();

		if (ScreenMouseEvents.ALLOW_MOUSE_SCROLL.invoker().allowMouseScroll(this.quilt$currentScreen, mouseX, mouseY, this.quilt$scrollDistanceX, scrollDistanceY) == TriState.FALSE) {
			this.quilt$currentScreen = null;
			this.quilt$scrollDistanceX = null;
			ci.cancel();
			return;
		}

		ScreenMouseEvents.BEFORE_MOUSE_SCROLL.invoker().beforeMouseScroll(this.quilt$currentScreen, mouseX, mouseY, this.quilt$scrollDistanceX, scrollDistanceY);
	}

	@Inject(method = "onMouseScroll", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;mouseScrolled(DDD)Z", shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
	private void afterMouseScrollEvent(long window, double scrollDeltaX, double scrollDeltaY, CallbackInfo ci, double scrollDistanceY, double mouseX, double mouseY) {
		if (this.quilt$currentScreen == null) {
			return;
		}

		ScreenMouseEvents.AFTER_MOUSE_SCROLL.invoker().afterMouseScroll(this.quilt$currentScreen, mouseX, mouseY, this.quilt$scrollDistanceX, scrollDistanceY);
		this.quilt$currentScreen = null;
		this.quilt$scrollDistanceX = null;
	}
}
