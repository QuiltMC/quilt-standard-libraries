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
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.Entity;
import net.minecraft.util.crash.CrashReportSection;

import org.quiltmc.qsl.crash.api.CrashReportEvents;

@Mixin(Entity.class)
public abstract class EntityMixin {
	@Inject(method = "populateCrashReport", at = @At("TAIL"))
	void addCrashReportDetails(CrashReportSection section, CallbackInfo ci) {
		CrashReportEvents.ENTITY_DETAILS.invoker().addDetails((Entity) (Object) this, section);
	}
}
