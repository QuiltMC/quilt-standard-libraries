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

package org.quiltmc.qsl.worldgen.surface_rule.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.unmapped.C_ircwepir;
import net.minecraft.world.gen.surfacebuilder.SurfaceRules;

/**
 * Represents a {@linkplain net.minecraft.world.gen.surfacebuilder.SurfaceRules.SequenceMaterialRule} that is easily reloadable when needed.
 */
@ApiStatus.Internal
public class ReloadableSequenceMaterialRule implements SurfaceRules.MaterialRule {
	static final C_ircwepir<ReloadableSequenceMaterialRule> RULE_CODEC = C_ircwepir.create(
			SurfaceRules.MaterialRule.CODEC
					.listOf()
					.xmap(ReloadableSequenceMaterialRule::new, ReloadableSequenceMaterialRule::sequence)
					.fieldOf("sequence")
	);

	private final List<SurfaceRules.MaterialRule> sequence;

	public ReloadableSequenceMaterialRule(List<SurfaceRules.MaterialRule> sequence) {
		this.sequence = new ArrayList<>(sequence);
	}

	public ReloadableSequenceMaterialRule() {
		this.sequence = new ArrayList<>();
	}

	public List<SurfaceRules.MaterialRule> sequence() {
		return this.sequence;
	}

	@Override
	public C_ircwepir<? extends SurfaceRules.MaterialRule> codec() {
		return RULE_CODEC;
	}

	@Override
	public SurfaceRules.SurfaceRule apply(SurfaceRules.Context context) {
		if (this.sequence.size() == 1) {
			return this.sequence.get(0).apply(context);
		} else {
			ImmutableList.Builder<SurfaceRules.SurfaceRule> builder = ImmutableList.builder();

			for (var materialRule : this.sequence) {
				builder.add(materialRule.apply(context));
			}

			return new SurfaceRules.SequenceRule(builder.build());
		}
	}
}
