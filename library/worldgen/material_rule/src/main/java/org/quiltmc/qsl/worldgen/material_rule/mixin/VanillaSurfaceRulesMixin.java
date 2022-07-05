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

package org.quiltmc.qsl.worldgen.material_rule.mixin;

import net.minecraft.world.gen.surfacebuilder.SurfaceRules;
import net.minecraft.world.gen.surfacebuilder.VanillaSurfaceRules;

import org.quiltmc.qsl.worldgen.material_rule.api.MaterialRuleRegistrationEvents;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

@Mixin(VanillaSurfaceRules.class)
public abstract class VanillaSurfaceRulesMixin {

    @Inject(
			method = "getOverworldRules",
			at = @At("RETURN"),
			cancellable = true)
    private static void quilt$injectOverworldRules(CallbackInfoReturnable<SurfaceRules.MaterialRule> cir) {
        ArrayList<SurfaceRules.MaterialRule> overworldMaterialRules = new ArrayList<>();

		MaterialRuleRegistrationEvents.OVERWORLD_RULE_INIT.invoker().registerRules(overworldMaterialRules);

		overworldMaterialRules.add(cir.getReturnValue());
		Logger LOGGER = LoggerFactory.getLogger(VanillaSurfaceRulesMixin.class);
		SurfaceRules.MaterialRule sequence = SurfaceRules.sequence(overworldMaterialRules.toArray(new SurfaceRules.MaterialRule[0]));
		LOGGER.info(String.valueOf(sequence));
        cir.setReturnValue(sequence);
    }

    @Inject(
			method = "getNetherRules",
			at = @At("RETURN"),
			cancellable = true)
    private static void quilt$injectNetherRules(CallbackInfoReturnable<SurfaceRules.MaterialRule> cir) {
		ArrayList<SurfaceRules.MaterialRule> netherMaterialRules = new ArrayList<>();

		MaterialRuleRegistrationEvents.NETHER_RULE_INIT.invoker().registerRules(netherMaterialRules);

		netherMaterialRules.add(cir.getReturnValue());

		cir.setReturnValue(SurfaceRules.sequence(netherMaterialRules.toArray(new SurfaceRules.MaterialRule[0])));
    }

	@Inject(
			method = "getNetherRules",
			at = @At("RETURN"),
			cancellable = true)
	private static void quiltInjectEndRules(CallbackInfoReturnable<SurfaceRules.MaterialRule> cir) {
		ArrayList<SurfaceRules.MaterialRule> theEndMaterialRules = new ArrayList<>();

		MaterialRuleRegistrationEvents.THE_END_RULE_INIT.invoker().registerRules(theEndMaterialRules);

		theEndMaterialRules.add(cir.getReturnValue());

		cir.setReturnValue(SurfaceRules.sequence(theEndMaterialRules.toArray(new SurfaceRules.MaterialRule[0])));
	}
}
