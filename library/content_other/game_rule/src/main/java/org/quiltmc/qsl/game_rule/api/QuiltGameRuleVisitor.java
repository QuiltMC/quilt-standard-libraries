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

package org.quiltmc.qsl.game_rule.api;

import net.minecraft.world.GameRules;

import org.quiltmc.qsl.game_rule.api.rule.DoubleRule;
import org.quiltmc.qsl.game_rule.api.rule.EnumRule;

/**
 * An extended game rule visitor which supports Quilt's own rule types.
 *
 * <p>Game rule visitors are typically used iterating all game rules.
 * In vanilla, the visitor is used to register game rule commands and populate the {@code Edit Game Rules} screen.
 *
 * <p>Rule types specified by this interface are not exhaustive.
 * New entries may be added in the future.
 */
public interface QuiltGameRuleVisitor extends GameRules.Visitor {
	/**
	 * Visit a double rule.
	 *
	 * <p>Note {@link #method_20762(GameRules.Key, GameRules.Type) visit(GameRules.Key, GameRules.Type)} will be called before this method is visited.
	 *
	 * @param key the rule key
	 * @param type the rule type
	 */
	default void visitDouble(GameRules.Key<DoubleRule> key, GameRules.Type<DoubleRule> type) {
	}

	/**
	 * Visit an enum rule.
	 *
	 * <p>Note {@link #method_20762(GameRules.Key, GameRules.Type) visit(GameRules.Key, GameRules.Type)} will be called before this method is visited.
	 *
	 * @param key the rule key
	 * @param type the rule type
	 */
	default <E extends Enum<E>> void visitEnum(GameRules.Key<EnumRule<E>> key, GameRules.Type<EnumRule<E>> type) {
	}
}
