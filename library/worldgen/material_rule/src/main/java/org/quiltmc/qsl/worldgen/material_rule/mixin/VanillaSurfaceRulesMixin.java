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
import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.worldgen.material_rule.api.MaterialRuleModifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;

/**
 * This class runs the {@code MaterialRuleModifier} to allow for modification of vanilla
 */
@Mixin(VanillaSurfaceRules.class)
public abstract class VanillaSurfaceRulesMixin {
	@Inject(
			method = "getOverworldLikeRules",
			at = @At("RETURN"),
			cancellable = true)
    private static void quilt$injectOverworldRules(boolean abovePreliminarySurface, boolean bedrockRoof, boolean bedrockFloor, CallbackInfoReturnable<SurfaceRules.MaterialRule> cir) {
		var list = new ArrayList<SurfaceRules.MaterialRule>();
		list.add(cir.getReturnValue());

		for (var ruleInit : QuiltLoader.getEntrypointContainers("qsl:material_rule_modifier", MaterialRuleModifier.class)) {
			ruleInit.getEntrypoint().addOverworldRules(abovePreliminarySurface, bedrockRoof, bedrockFloor, list);
		}

		cir.setReturnValue(SurfaceRules.sequence(list.toArray(new SurfaceRules.MaterialRule[0])));
    }

    @Inject(
			method = "getNetherRules",
			at = @At("RETURN"),
			cancellable = true)
    private static void quilt$injectNetherRules(CallbackInfoReturnable<SurfaceRules.MaterialRule> cir) {
		var list = new ArrayList<SurfaceRules.MaterialRule>();
		list.add(cir.getReturnValue());

		for (var ruleInit : QuiltLoader.getEntrypointContainers("qsl:material_rule_modifier", MaterialRuleModifier.class)) {
			ruleInit.getEntrypoint().addNetherRules(list);
		}

		cir.setReturnValue(SurfaceRules.sequence(list.toArray(new SurfaceRules.MaterialRule[0])));
    }

	@Inject(
			method = "getEndRules",
			at = @At("RETURN"),
			cancellable = true)
	private static void quilt$injectEndRules(CallbackInfoReturnable<SurfaceRules.MaterialRule> cir) {
		var list = new ArrayList<SurfaceRules.MaterialRule>();
		list.add(cir.getReturnValue());

		for (var ruleInit : QuiltLoader.getEntrypointContainers("qsl:material_rule_modifier", MaterialRuleModifier.class)) {
			ruleInit.getEntrypoint().addEndRules(list);
		}

		cir.setReturnValue(SurfaceRules.sequence(list.toArray(new SurfaceRules.MaterialRule[0])));
	}

}
