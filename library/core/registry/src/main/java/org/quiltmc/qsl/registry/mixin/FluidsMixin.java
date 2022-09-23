/*
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.qsl.registry.mixin;

import com.mojang.logging.LogUtils;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.registry.api.event.RegistryEvents;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Fluids.class)
public abstract class FluidsMixin {
	private static final Logger quilt$LOGGER = LogUtils.getLogger();

	@Inject(method = "<clinit>", at = @At("RETURN"))
	private static void onInit(CallbackInfo ci) {
		RegistryEvents.getEntryAddEvent(Registry.FLUID).register(context -> context.value().getStateManager().getStates().forEach((state) -> {
			if (Fluid.STATE_IDS.getRawId(state) == -1) {
				Fluid.STATE_IDS.add(state);
			} else {
				quilt$LOGGER.warn("FluidState " + state.toString() + " has been added twice!");
			}
		}));
	}
}
