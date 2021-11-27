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

package org.quiltmc.qsl.loot.api.event;

import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.event.ArrayEvent;
import org.quiltmc.qsl.loot.api.QuiltLootTableBuilder;

/**
 * An event handler that is called when loot tables are loaded.
 * Use {@link #EVENT} to register instances.
 */
@FunctionalInterface
public interface LootTableLoadingCallback {
	class Context {
		public final ResourceManager resourceManager;
		public final LootManager manager;
		public final Identifier id;
		public final QuiltLootTableBuilder table;

		private Context(ResourceManager resourceManager, LootManager manager, Identifier id, QuiltLootTableBuilder table) {
			this.resourceManager = resourceManager;
			this.manager = manager;
			this.id = id;
			this.table = table;
		}

		public static Context create(ResourceManager resourceManager, LootManager manager, Identifier id, QuiltLootTableBuilder table) {
			return new Context(resourceManager, manager, id, table);
		}
	}

	@FunctionalInterface
	interface LootTableSetter {
		void set(LootTable table);
	}

	ArrayEvent<LootTableLoadingCallback> EVENT = ArrayEvent.create(
			LootTableLoadingCallback.class,
			(listeners) -> (context, setter) -> {
				for (LootTableLoadingCallback callback : listeners) {
					callback.onLootTableLoading(context, setter);
				}
			}
	);

	/**
	 * Called when a loot table is loaded.
	 *
	 * @param context the context for the event
	 * @param setter a function that can be used to set the loot table
	 */
	void onLootTableLoading(Context context, LootTableSetter setter);
}
