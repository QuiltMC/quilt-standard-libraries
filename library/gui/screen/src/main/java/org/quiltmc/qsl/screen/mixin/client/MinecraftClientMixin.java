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

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.screen.api.client.ScreenEvents;

@ClientOnly
@Mixin(MinecraftClient.class)
abstract class MinecraftClientMixin {
	@Shadow
	public Screen currentScreen;

	@Unique
	private Screen quilt$tickingScreen;

	@Inject(method = "setScreen", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;removed()V", shift = At.Shift.AFTER))
	private void onScreenRemove(@Nullable Screen screen, CallbackInfo ci) {
		ScreenEvents.REMOVE.invoker().onRemove(this.currentScreen);
	}

	@Inject(method = "stop", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;removed()V", shift = At.Shift.AFTER))
	private void onScreenRemoveBecauseStopping(CallbackInfo ci) {
		ScreenEvents.REMOVE.invoker().onRemove(this.currentScreen);
	}

	// Synthetic method method_1572()V -> lambda in Screen.wrapScreenError in MinecraftClient.tick
	// These two injections should be caught by "Screen#wrapScreenError" if anything fails in an event and then rethrown in the crash report
	@Inject(method = "method_1572()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;tick()V"))
	private void beforeScreenTick(CallbackInfo ci) {
		// Store the screen in a variable in case someone tries to change the screen during this before tick event.
		// If someone changes the screen, the after tick event will likely have class cast exceptions or an NPE.
		this.quilt$tickingScreen = this.currentScreen;
		ScreenEvents.BEFORE_TICK.invoker().beforeTick(this.quilt$tickingScreen);
	}

	// Synthetic method method_1572()V -> lambda in Screen.wrapScreenError in MinecraftClient.tick
	@Inject(method = "method_1572()V", at = @At("TAIL"))
	private void afterScreenTick(CallbackInfo ci) {
		ScreenEvents.AFTER_TICK.invoker().afterTick(this.quilt$tickingScreen);
		// Finally set the currently ticking screen to null
		this.quilt$tickingScreen = null;
	}

	// The LevelLoadingScreen is the odd screen that isn't ticked by the main tick loop, so we fire events for this screen.
	@Inject(method = "startIntegratedServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/WorldLoadingScreen;tick()V"))
	private void beforeLoadingScreenTick(CallbackInfo ci) {
		// Store the screen in a variable in case someone tries to change the screen during this before tick event.
		// If someone changes the screen, the after tick event will likely have class cast exceptions or throw a NPE.
		this.quilt$tickingScreen = this.currentScreen;
		ScreenEvents.BEFORE_TICK.invoker().beforeTick(this.quilt$tickingScreen);
	}

	@Inject(method = "startIntegratedServer", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;render(Z)V"))
	private void afterLoadingScreenTick(CallbackInfo ci) {
		ScreenEvents.AFTER_TICK.invoker().afterTick(this.quilt$tickingScreen);
		// Finally set the currently ticking screen to null
		this.quilt$tickingScreen = null;
	}
}
