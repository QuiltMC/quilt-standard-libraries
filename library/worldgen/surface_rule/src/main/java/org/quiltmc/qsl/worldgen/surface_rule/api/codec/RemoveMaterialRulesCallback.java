/*
 * Copyright 2023 QuiltMC
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

package org.quiltmc.qsl.worldgen.surface_rule.api.codec;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;
import net.minecraft.world.gen.surfacebuilder.SurfaceRules;

import org.quiltmc.qsl.worldgen.surface_rule.api.SurfaceRuleContext;
import org.quiltmc.qsl.worldgen.surface_rule.api.SurfaceRuleEvents;

/**
 * A surface rule callback that removes a material rule from a surface rule's list of material rules.
 * @param rule a rule which should be matched exactly by the rule to remove
 */
public record RemoveMaterialRulesCallback(SurfaceRules.MaterialRule rule) implements SurfaceRuleEvents.OverworldModifierCallback,
		SurfaceRuleEvents.NetherModifierCallback,
		SurfaceRuleEvents.TheEndModifierCallback,
		SurfaceRuleEvents.GenericModifierCallback {

	public static final Codec<RemoveMaterialRulesCallback> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SurfaceRules.MaterialRule.CODEC.fieldOf("rule").forGetter(RemoveMaterialRulesCallback::rule)
	).apply(instance, RemoveMaterialRulesCallback::new));

	public static final Identifier IDENTIFIER = new Identifier("quilt", "remove_material_rule");

	@Override
	public void modifyOverworldRules(SurfaceRuleContext.@NotNull Overworld context) {
		modifySurfaceRules(context);
	}

	@Override
	public void modifyNetherRules(SurfaceRuleContext.@NotNull Nether context) {
		modifySurfaceRules(context);
	}

	@Override
	public void modifyTheEndRules(SurfaceRuleContext.@NotNull TheEnd context) {
		modifySurfaceRules(context);
	}

	@Override
	public void modifyGenericSurfaceRules(@NotNull SurfaceRuleContext context) {
		modifySurfaceRules(context);
	}

	private void modifySurfaceRules(@NotNull SurfaceRuleContext context) {
		context.materialRules().removeIf(rule::equals);
	}

	@Override
	public Identifier getCodecIdentifier() {
		return IDENTIFIER;
	}
}
