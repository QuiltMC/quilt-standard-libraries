/*
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

package org.quiltmc.qsl.fluid.mixin;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.FluidState;
import org.quiltmc.qsl.fluid.api.QuiltFlowableFluidExtensions;
import org.quiltmc.qsl.fluid.impl.CameraExtensions;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {

	@Shadow
	private static float red;
	@Shadow
	private static float green;
	@Shadow
	private static float blue;
	@Shadow
	private static long lastWaterFogColorUpdateTime = -1L;

	@Inject(method = "render(Lnet/minecraft/client/render/Camera;FLnet/minecraft/client/world/ClientWorld;IF)V",
			at = @At("HEAD"),
			cancellable = true)
	private static void render(Camera camera, float tickDelta, ClientWorld world, int i, float f, CallbackInfo ci) {
		//Get the fluid that submerged the camera
		FluidState fluidState = ((CameraExtensions) camera).quilt$getSubmergedFluidState();

		//If this is an instance of QuiltFlowableFluidExtensions interface...
		if (fluidState.getFluid() instanceof QuiltFlowableFluidExtensions fluid) {
			//Get the color of the fog...
			int fogColor = fluid.getFogColor(fluidState, camera.getFocusedEntity());
			if (fogColor != -1) { // water color special casing, -1 marks water color
				//This is a hexadecimal color, so we need to get the three "red", "green", and "blue" values.
				red = (fogColor >> 16 & 255) / 255f;
				green = (fogColor >> 8 & 255) / 255f;
				blue = (fogColor & 255) / 255f;

				//This is for compatibility, just add!
				lastWaterFogColorUpdateTime = -1L;

				//Apply the color, then return.
				RenderSystem.clearColor(red, green, blue, 0.0f);

				ci.cancel();
			}
		}
	}

	@Inject(method = "applyFog",
			at = @At("HEAD"),
			cancellable = true)
	private static void applyFog(Camera camera, BackgroundRenderer.FogType fogType, float viewDistance, boolean thickFog, float tickDelta, CallbackInfo ci) {
		//Get the fluid that submerged the camera
		FluidState fluidState = ((CameraExtensions) camera).quilt$getSubmergedFluidState();

		//If this is an instance of QuiltFlowableFluidExtensions interface...
		if (fluidState.getFluid() instanceof QuiltFlowableFluidExtensions fluid) {

			//Get the start and end parameters and apply them, then return.
			Entity entity = camera.getFocusedEntity();
			RenderSystem.setShaderFogStart(fluid.getFogStart(fluidState, entity, viewDistance));
			RenderSystem.setShaderFogEnd(fluid.getFogEnd(fluidState, entity, viewDistance));

			ci.cancel();
		}
	}

}
