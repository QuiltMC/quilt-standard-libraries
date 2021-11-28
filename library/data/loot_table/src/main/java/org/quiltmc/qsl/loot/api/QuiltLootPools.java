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

import java.util.List;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.provider.number.LootNumberProvider;

import org.quiltmc.qsl.loot.mixin.LootPoolAccessor;

/**
 * Utility methods for working with {@link LootPool}s.
 *
 * <p>Contains accessors for various fields.
 */
public class QuiltLootPools {
	private QuiltLootPools() {
	}

	/**
	 * Gets the entries in the given {@code pool} in an immutable list.
	 *
	 * @return the entries in a pool
	 */
	public static List<LootPoolEntry> getEntries(@NotNull LootPool pool) {
		Objects.requireNonNull(pool, "pool must not be null");
		return ImmutableList.copyOf(((LootPoolAccessor) pool).getEntries());
	}

	/**
	 * Gets the conditions of the given {@code pool} in an immutable list.
	 *
	 * @return the conditions of a pool
	 */
	public static List<LootCondition> getConditions(@NotNull LootPool pool) {
		Objects.requireNonNull(pool, "pool must not be null");
		return ImmutableList.copyOf(((LootPoolAccessor) pool).getConditions());
	}

	/**
	 * Gets the functions in the given {@code pool} in an immutable list.
	 *
	 * @return the functions applied to a pool
	 */
	public static List<LootFunction> getFunctions(@NotNull LootPool pool) {
		Objects.requireNonNull(pool, "pool must not be null");
		return ImmutableList.copyOf(((LootPoolAccessor) pool).getFunctions());
	}

	/**
	 * Gets the rolls of the given {@code pool}, specifying how many entries from the pool should
	 * be selected.
	 *
	 * @return the rolls of a pool
	 */
	public static LootNumberProvider getRolls(@NotNull LootPool pool) {
		Objects.requireNonNull(pool, "pool must not be null");
		return ((LootPoolAccessor) pool).getRolls();
	}

	/**
	 * Gets the bonus rolls of the given {@code pool}, specifying the number of additional
	 * {@linkplain #getRolls(LootPool) rolls} per point of
	 * {@linkplain net.minecraft.loot.context.LootContext#getLuck() luck}.
	 *
	 * @return the bonus rolls of a pool
	 * @see #getRolls(LootPool)
	 */
	public static LootNumberProvider getBonusRolls(@NotNull LootPool pool) {
		Objects.requireNonNull(pool, "pool must not be null");
		return ((LootPoolAccessor) pool).getBonusRolls();
	}
}
