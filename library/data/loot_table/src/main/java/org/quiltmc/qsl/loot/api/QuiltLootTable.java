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
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.loot.function.LootFunction;

import java.util.List;

/**
 * Interface implemented by all {@link net.minecraft.loot.LootTable} instances when QSL is present.
 * <p>
 * Contains accessors for various fields.
 *
 * @see	QuiltLootPoolBuilder
 * @see #cast(LootTable)
 */
public interface QuiltLootTable {
	List<LootPool> getPools();

	List<LootFunction> getFunctions();

	LootContextType getType();

	static QuiltLootTable cast(LootTable table) {
		return (QuiltLootTable) table;
	}
}
