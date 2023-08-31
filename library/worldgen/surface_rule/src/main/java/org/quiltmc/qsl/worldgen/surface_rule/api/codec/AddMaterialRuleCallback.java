/*
 * Copyright 2023 The Quilt Project
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

import java.util.List;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;
import net.minecraft.world.gen.surfacebuilder.SurfaceRules;

import org.quiltmc.qsl.data.callback.api.CodecHelpers;
import org.quiltmc.qsl.worldgen.surface_rule.api.SurfaceRuleContext;
import org.quiltmc.qsl.worldgen.surface_rule.api.SurfaceRuleEvents;

/**
 * A surface rule callback that adds a material rule to a surface rule's list of material rules.
 *
 * @param rule   the material rule to add
 * @param append if {@code true}, the rule will be added to the end of the list of material rules. If {@code false}, the rule will be added to the beginning.
 * @param ids    one or more chunk generator settings identifiers which this callback should be applied to. If not present,
 *               the callback will be applied to all chunk generator settings.
 */
public record AddMaterialRuleCallback(
		SurfaceRules.MaterialRule rule, boolean append, Optional<List<Identifier>> ids
) implements SurfaceRuleEvents.OverworldModifierCallback, SurfaceRuleEvents.NetherModifierCallback, SurfaceRuleEvents.TheEndModifierCallback,
		SurfaceRuleEvents.GenericModifierCallback {
	public static final Identifier CODEC_ID = new Identifier("quilt", "add_material_rule");
	public static final Codec<AddMaterialRuleCallback> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			SurfaceRules.MaterialRule.CODEC.fieldOf("rule").forGetter(AddMaterialRuleCallback::rule),
			Codec.BOOL.optionalFieldOf("append", false).forGetter(AddMaterialRuleCallback::append),
			CodecHelpers.listOrValue(Identifier.CODEC).optionalFieldOf("ids").forGetter(AddMaterialRuleCallback::ids)
	).apply(instance, AddMaterialRuleCallback::new));

	@Override
	public Identifier getCodecId() {
		return CODEC_ID;
	}

	@Override
	public void modifyGenericSurfaceRules(@NotNull SurfaceRuleContext context) {
		this.modifyRules(context);
	}

	@Override
	public void modifyOverworldRules(SurfaceRuleContext.@NotNull Overworld context) {
		this.modifyRules(context);
	}

	@Override
	public void modifyNetherRules(SurfaceRuleContext.@NotNull Nether context) {
		this.modifyRules(context);
	}

	@Override
	public void modifyTheEndRules(SurfaceRuleContext.@NotNull TheEnd context) {
		this.modifyRules(context);
	}

	private void modifyRules(SurfaceRuleContext context) {
		if (this.ids().isPresent() && !this.ids().get().contains(context.identifier())) {
			return;
		}

		if (this.append) {
			context.materialRules().add(this.rule);
		} else {
			context.materialRules().add(0, this.rule);
		}
	}
}
