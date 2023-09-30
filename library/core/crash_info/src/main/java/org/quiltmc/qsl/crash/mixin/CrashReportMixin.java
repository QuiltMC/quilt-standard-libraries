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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.util.crash.CrashReport;

import org.quiltmc.qsl.crash.api.CrashReportEvents;

@Mixin(CrashReport.class)
public abstract class CrashReportMixin {
	@Unique
	private boolean quilt$firedEvent = false;

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "addStackTrace", at = @At(value = "HEAD"))
	void onCrashReportCreated(StringBuilder crashReportBuilder, CallbackInfo ci) {
		if (!this.quilt$firedEvent) {
			CrashReportEvents.CRASH_REPORT_CREATION.invoker().onCreate((CrashReport) (Object) this);
			this.quilt$firedEvent = true;
		}
	}
}
