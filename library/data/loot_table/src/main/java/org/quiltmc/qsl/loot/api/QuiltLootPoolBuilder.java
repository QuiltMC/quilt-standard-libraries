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

import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.provider.number.LootNumberProvider;
import org.quiltmc.qsl.loot.mixin.LootPoolBuilderHooks;

public class QuiltLootPoolBuilder extends LootPool.Builder {
	private final LootPoolBuilderHooks extended = (LootPoolBuilderHooks) this;

	private QuiltLootPoolBuilder() {
	}

	private QuiltLootPoolBuilder(LootPool pool) {
		copyFrom(pool, true);
	}

	@Override
	public QuiltLootPoolBuilder rolls(LootNumberProvider rolls) {
		super.rolls(rolls);
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

	public QuiltLootPoolBuilder withEntry(LootPoolEntry entry) {
		extended.getEntries().add(entry);
		return this;
	}

	public QuiltLootPoolBuilder withCondition(LootCondition condition) {
		extended.getConditions().add(condition);
		return this;
	}

	public QuiltLootPoolBuilder withFunction(LootFunction function) {
		extended.getFunctions().add(function);
		return this;
	}

	/**
	 * Copies the entries, conditions and functions of the {@code pool} to this
	 * builder.
	 *
	 * <p>This is equal to {@code copyFrom(pool, false)}.
	 */
	public QuiltLootPoolBuilder copyFrom(LootPool pool) {
		return copyFrom(pool, false);
	}

	/**
	 * Copies the entries, conditions and functions of the {@code pool} to this
	 * builder.
	 *
	 * <p>If {@code copyRolls} is true, the {@link QuiltLootPool#getRolls rolls} of the pool are also copied.
	 */
	public QuiltLootPoolBuilder copyFrom(LootPool pool, boolean copyRolls) {
		QuiltLootPool extendedPool = (QuiltLootPool) pool;
		extended.getConditions().addAll(extendedPool.getConditions());
		extended.getFunctions().addAll(extendedPool.getFunctions());
		extended.getEntries().addAll(extendedPool.getEntries());

		if (copyRolls) {
			rolls(extendedPool.getRolls());
		}

		return this;
	}

	public static QuiltLootPoolBuilder builder() {
		return new QuiltLootPoolBuilder();
	}

	public static QuiltLootPoolBuilder of(LootPool pool) {
		return new QuiltLootPoolBuilder(pool);
	}
}
