/*
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

package org.quiltmc.qsl.items.itemgroup.test;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import net.fabricmc.api.ModInitializer;
import org.quiltmc.qsl.itemgroup.api.QuiltItemGroupBuilder;

public class ItemGroupTest implements ModInitializer {
	//Adds an item group with all items in it
	private static final ItemGroup ITEM_GROUP = QuiltItemGroupBuilder.create(new Identifier("qsl-items-item-group-testmod", "test_group"))
				.icon(() -> new ItemStack(Items.DIAMOND))
				.appendItems(stacks ->
						Registry.ITEM.stream()
						.map(ItemStack::new)
						.forEach(stacks::add)
				).build();

	@Override
	public void onInitialize() {
	}
}
