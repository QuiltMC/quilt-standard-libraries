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

import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import com.mojang.blaze3d.glfw.Window;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.screen.api.client.ScreenEvents;

@ClientOnly
@Mixin(GameRenderer.class)
abstract class GameRendererMixin {
	@Shadow
	@Final
	MinecraftClient client;

	@Unique
	private Screen quilt$renderingScreen;

	@SuppressWarnings("InvalidInjectorMethodSignature")
	@Inject(
			method = "render",
			at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screen/Screen;renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V"),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onBeforeRenderScreen(float tickDelta, long startTime, boolean tick, CallbackInfo ci,
									  int mouseX, int mouseY, Window window, Matrix4f projectionMatrix, MatrixStack matrices, GuiGraphics graphics) {
		// Store the screen in a variable in case someone tries to change the screen during this before render event.
		// If someone changes the screen, the after render event will likely have class cast exceptions or an NPE.
		this.quilt$renderingScreen = this.client.currentScreen;
		ScreenEvents.BEFORE_RENDER.invoker().beforeRender(this.quilt$renderingScreen, graphics, mouseX, mouseY, tickDelta);
	}

	// This injection should end up in the try block so exceptions are caught
	@SuppressWarnings("InvalidInjectorMethodSignature")
	@Inject(
			method = "render",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/screen/Screen;renderWithTooltip(Lnet/minecraft/client/gui/GuiGraphics;IIF)V",
					shift = At.Shift.AFTER
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onAfterRenderScreen(float tickDelta, long startTime, boolean tick, CallbackInfo ci,
									 int mouseX, int mouseY, Window window, Matrix4f projectionMatrix, MatrixStack matrices, GuiGraphics graphics) {
		ScreenEvents.AFTER_RENDER.invoker().afterRender(this.quilt$renderingScreen, graphics, mouseX, mouseY, tickDelta);
		// Finally set the currently rendering screen to null
		this.quilt$renderingScreen = null;
	}
}
