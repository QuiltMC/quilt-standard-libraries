/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.game_rule.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.GameRules;

import org.quiltmc.qsl.game_rule.api.CustomGameRuleCategory;
import org.quiltmc.qsl.game_rule.impl.RuleKeyExtensions;

@Mixin(GameRules.Key.class)
public abstract class RuleKeyMixin implements RuleKeyExtensions {
	@Unique
	@Nullable
	private CustomGameRuleCategory customCategory;

	@Override
	public CustomGameRuleCategory quilt$getCustomCategory() {
		return this.customCategory;
	}

	@Override
	public void quilt$setCustomCategory(CustomGameRuleCategory customCategory) {
		this.customCategory = customCategory;
	}
}
