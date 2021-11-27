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

import org.quiltmc.qsl.loot.mixin.LootTableBuilderHooks;

/**
 * Quilt's version of {@link net.minecraft.loot.LootTable.Builder}. Adds additional methods and
 * hooks not found in the original class.
 *
 * <p>To create a new instance of this class, use {@link #builder()}.
 *
 * @see #copyFrom(LootTable)
 * @see #copyFrom(LootTable, boolean)
 */
public class QuiltLootTableBuilder extends LootTable.Builder {
	private final LootTableBuilderHooks extended = (LootTableBuilderHooks) this;

	private QuiltLootTableBuilder() {
	}

	private QuiltLootTableBuilder(LootTable table) {
		copyFrom(table, true);
	}

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

	public QuiltLootTableBuilder withPool(LootPool pool) {
		extended.getPools().add(pool);
		return this;
	}

	public QuiltLootTableBuilder withFunction(LootFunction function) {
		extended.getFunctions().add(function);
		return this;
	}

	public QuiltLootTableBuilder withPools(Collection<LootPool> pools) {
		pools.forEach(this::withPool);
		return this;
	}

	public QuiltLootTableBuilder withFunctions(Collection<LootFunction> functions) {
		functions.forEach(this::withFunction);
		return this;
	}

	/**
	 * Copies the pools and functions of the {@code table} to this builder.
	 * This is equal to {@code copyFrom(table, false)}.
	 */
	public QuiltLootTableBuilder copyFrom(LootTable table) {
		return copyFrom(table, false);
	}

	/**
	 * Copies the pools and functions of the {@code table} to this builder.
	 * If {@code copyType} is true, the {@link QuiltLootTable#getType type} of the table is also copied.
	 */
	public QuiltLootTableBuilder copyFrom(LootTable table, boolean copyType) {
		QuiltLootTable extendedTable = (QuiltLootTable) table;
		extended.getPools().addAll(extendedTable.getPools());
		extended.getFunctions().addAll(extendedTable.getFunctions());

		if (copyType) {
			type(extendedTable.getType());
		}

		return this;
	}

	public static QuiltLootTableBuilder builder() {
		return new QuiltLootTableBuilder();
	}

	public static QuiltLootTableBuilder of(LootTable table) {
		return new QuiltLootTableBuilder(table);
	}
}
