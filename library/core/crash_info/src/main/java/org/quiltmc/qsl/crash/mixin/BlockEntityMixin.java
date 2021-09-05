/*
 * Copyright 2021 QuiltMC
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

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.crash.CrashReportSection;
import org.quiltmc.qsl.crash.api.CrashReportEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin {
	@Inject(method = "populateCrashReport", at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/util/crash/CrashReportSection;add(Ljava/lang/String;Lnet/minecraft/util/crash/CrashCallable;)Lnet/minecraft/util/crash/CrashReportSection;"), locals = LocalCapture.CAPTURE_FAILHARD)
	void addCrashReportDetails(CrashReportSection section, CallbackInfo ci) {
		CrashReportEvents.BLOCKENTITY_DETAILS.invoker().addDetails((BlockEntity) (Object) this, section);
	}
}
