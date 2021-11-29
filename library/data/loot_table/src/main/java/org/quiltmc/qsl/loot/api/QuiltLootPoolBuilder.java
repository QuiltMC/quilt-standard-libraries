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

package org.quiltmc.qsl.loot.api;

import java.util.Collection;

import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.provider.number.LootNumberProvider;

import org.quiltmc.qsl.loot.mixin.LootPoolBuilderAccessor;

/**
 * Quilt's version of {@link net.minecraft.loot.LootPool.Builder}. Adds additional methods and
 * hooks not found in the original class.
 *
 * <p>To create a new instance of this class, use {@link #builder()}.
 *
 * @see #copyOf(LootPool)
 */
public class QuiltLootPoolBuilder extends LootPool.Builder {
	private final LootPoolBuilderAccessor access = (LootPoolBuilderAccessor) this;

	private QuiltLootPoolBuilder() {
	}

	// Vanilla overrides

	@Override
	public QuiltLootPoolBuilder rolls(LootNumberProvider rolls) {
		super.rolls(rolls);
		return this;
	}

	@Override
	public LootPool.Builder bonusRolls(LootNumberProvider bonusRolls) {
		super.bonusRolls(bonusRolls);
		return this;
	}

	@Override
	public QuiltLootPoolBuilder with(LootPoolEntry.Builder<?> entry) {
		super.with(entry);
		return this;
	}

	@Override
	public QuiltLootPoolBuilder conditionally(LootCondition.Builder builder) {
		super.conditionally(builder);
		return this;
	}

	@Override
	public QuiltLootPoolBuilder apply(LootFunction.Builder builder) {
		super.apply(builder);
		return this;
	}

	// Additional methods

	/**
	 * Adds an entry to this builder.
	 *
	 * @param entry the entry to add
	 * @return this builder
	 */
	public QuiltLootPoolBuilder with(LootPoolEntry entry) {
		access.getEntries().add(entry);
		return this;
	}

	/**
	 * Adds entries to this builder.
	 *
	 * @param entries the entries to add
	 * @return this builder
	 */
	public QuiltLootPoolBuilder with(Collection<? extends LootPoolEntry> entries) {
		access.getEntries().addAll(entries);
		return this;
	}

	/**
	 * Adds a condition to this builder.
	 *
	 * @param condition the condition to add
	 * @return this builder
	 */
	public QuiltLootPoolBuilder conditionally(LootCondition condition) {
		access.getConditions().add(condition);
		return this;
	}

	/**
	 * Adds conditions to this builder.
	 *
	 * @param conditions the conditions to add
	 * @return this builder
	 */
	public QuiltLootPoolBuilder conditionally(Collection<? extends LootCondition> conditions) {
		access.getConditions().addAll(conditions);
		return this;
	}

	/**
	 * Applies a function to this builder.
	 *
	 * @param function the function to apply
	 * @return this builder
	 */
	public QuiltLootPoolBuilder apply(LootFunction function) {
		access.getFunctions().add(function);
		return this;
	}

	/**
	 * Applies functions to this builder.
	 *
	 * @param functions the functions to apply
	 * @return this builder
	 */
	public QuiltLootPoolBuilder apply(Collection<? extends LootFunction> functions) {
		access.getFunctions().addAll(functions);
		return this;
	}

	/**
	 * Creates an empty loot pool builder.
	 *
	 * @return the created builder
	 */
	public static QuiltLootPoolBuilder builder() {
		return new QuiltLootPoolBuilder();
	}

	/**
	 * Creates a builder copy of the given loot pool.
	 *
	 * @param pool the pool to copy
	 * @return the copied builder
	 */
	public static QuiltLootPoolBuilder copyOf(LootPool pool) {
		QuiltLootPoolBuilder builder = new QuiltLootPoolBuilder();

		builder.rolls(QuiltLootPools.getRolls(pool));
		builder.bonusRolls(QuiltLootPools.getBonusRolls(pool));
		builder.with(QuiltLootPools.getEntries(pool));
		builder.conditionally(QuiltLootPools.getConditions(pool));
		builder.apply(QuiltLootPools.getFunctions(pool));

		return builder;
	}
}
