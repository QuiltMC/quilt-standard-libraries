/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.crash.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.util.crash.CrashReportSection;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.HeightLimitView;

import org.quiltmc.qsl.crash.api.CrashReportEvents;

@Mixin(CrashReportSection.class)
public abstract class CrashReportSectionMixin {
	@Shadow
	private StackTraceElement[] stackTrace;

	@Inject(method = "addBlockInfo", at = @At("TAIL"))
	private static void addCrashReportDetails(CrashReportSection element, HeightLimitView world, BlockPos pos, BlockState state, CallbackInfo ci) {
		CrashReportEvents.BLOCK_DETAILS.invoker().addDetails(world, pos, state, element);
	}

	@Inject(method = "trimStackTraceEnd", at = @At("HEAD"), cancellable = true)
	private void onTrimStackTraceEnd(int callCount, CallbackInfo ci) {
		// Fix sections added by mods potentially messing up with the stack trace output.
		if (this.stackTrace.length - callCount < 0) {
			ci.cancel();
		}
	}
}
