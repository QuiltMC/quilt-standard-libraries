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
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.function.LootFunction;

import org.quiltmc.qsl.loot.mixin.LootTableBuilderAccessor;

/**
 * Quilt's version of {@link net.minecraft.loot.LootTable.Builder}. Adds additional methods and
 * hooks not found in the original class.
 *
 * <p>To create a new instance of this class, use {@link #builder()}.
 *
 * @see #copyOf(LootTable)
 */
public class QuiltLootTableBuilder extends LootTable.Builder {
	private final LootTableBuilderAccessor access = (LootTableBuilderAccessor) this;

	private QuiltLootTableBuilder() {
	}

	// Vanilla overrides

	@Override
	public QuiltLootTableBuilder pool(LootPool.Builder pool) {
		super.pool(pool);
		return this;
	}

	@Override
	public QuiltLootTableBuilder type(LootContextType type) {
		super.type(type);
		return this;
	}

	@Override
	public QuiltLootTableBuilder apply(LootFunction.Builder function) {
		super.apply(function);
		return this;
	}

	// Additional methods

	/**
	 * Adds a pool to this builder.
	 *
	 * @param pool the pool to add
	 * @return this builder
	 */
	public QuiltLootTableBuilder pool(LootPool pool) {
		access.getPools().add(pool);
		return this;
	}

	/**
	 * Adds pools to this builder.
	 *
	 * @param pools the pools to add
	 * @return this builder
	 */
	public QuiltLootTableBuilder pools(Collection<? extends LootPool> pools) {
		access.getPools().addAll(pools);
		return this;
	}

	/**
	 * Applies a function to this builder.
	 *
	 * @param function the function to apply
	 * @return this builder
	 */
	public QuiltLootTableBuilder apply(LootFunction function) {
		access.getFunctions().add(function);
		return this;
	}

	/**
	 * Applies functions to this builder.
	 *
	 * @param functions the functions to apply
	 * @return this builder
	 */
	public QuiltLootTableBuilder apply(Collection<? extends LootFunction> functions) {
		access.getFunctions().addAll(functions);
		return this;
	}

	/**
	 * Creates an empty loot table builder.
	 *
	 * @return the created builder
	 */
	public static QuiltLootTableBuilder builder() {
		return new QuiltLootTableBuilder();
	}

	/**
	 * Creates a builder copy of the given loot table.
	 *
	 * @param table the table to copy
	 * @return the copied builder
	 */
	public static QuiltLootTableBuilder copyOf(LootTable table) {
		QuiltLootTableBuilder builder = new QuiltLootTableBuilder();

		builder.type(table.getType());
		builder.pools(QuiltLootTables.getPools(table));
		builder.apply(QuiltLootTables.getFunctions(table));

		return builder;
	}
}
