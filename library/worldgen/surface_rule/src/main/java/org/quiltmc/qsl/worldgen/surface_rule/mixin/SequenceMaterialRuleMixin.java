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

package org.quiltmc.qsl.worldgen.surface_rule.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.world.gen.surfacebuilder.SurfaceRules;

import org.quiltmc.qsl.worldgen.surface_rule.impl.QuiltSequenceMaterialRuleHooks;

@Mixin(SurfaceRules.SequenceMaterialRule.class)
public class SequenceMaterialRuleMixin implements QuiltSequenceMaterialRuleHooks {
	@Mutable
	@Shadow
	@Final
	private List<SurfaceRules.MaterialRule> sequence;

	@Override
	public void quilt$freeze() {
		this.sequence = List.copyOf(this.sequence);
	}
}
