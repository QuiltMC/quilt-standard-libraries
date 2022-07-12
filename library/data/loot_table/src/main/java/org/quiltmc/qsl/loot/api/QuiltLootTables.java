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
import net.minecraft.loot.LootTable;
import net.minecraft.loot.function.LootFunction;

import org.quiltmc.qsl.loot.mixin.LootTableAccessor;

/**
 * Utility methods for working with {@link LootTable}s.
 *
 * <p>Contains accessors for various fields.
 */
public class QuiltLootTables {
	private QuiltLootTables() {
	}

	/**
	 * Gets the pools in the given {@code table} in an immutable list.
	 *
	 * @return the pools in a loot table
	 */
	public static List<LootPool> getPools(@NotNull LootTable table) {
		Objects.requireNonNull(table, "table must not be null");
		return ImmutableList.copyOf(((LootTableAccessor) table).getPools());
	}

	/**
	 * Gets the functions in the given {@code table} in an immutable list.
	 *
	 * @return the functions in a loot table
	 */
	public static List<LootFunction> getFunctions(@NotNull LootTable table) {
		Objects.requireNonNull(table, "table must not be null");
		return ImmutableList.copyOf(((LootTableAccessor) table).getFunctions());
	}
}
