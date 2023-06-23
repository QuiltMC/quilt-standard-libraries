/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.item.group.test;

import java.util.stream.IntStream;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.group.api.QuiltItemGroup;

public class ItemGroupTest implements ModInitializer {
	public static final String NAMESPACE = "quilt_item_group_testmod";
	// Adds an item group with all items in it
	private static final ItemGroup SUPPLIER_ITEM_GROUP = QuiltItemGroup.builder(new Identifier(NAMESPACE, "test_supplied_group"))
			.icon(() -> new ItemStack(Items.STONE))
			.appendItems(stacks ->
					Registry.ITEM.stream()
							.map(ItemStack::new)
							.filter(itemStack -> itemStack.toString().contains("stone"))
							.forEach(stacks::add)
			).build();

	private static final QuiltItemGroup DELAYED_ITEM_GROUP = QuiltItemGroup.builder(new Identifier(NAMESPACE, "test_delayed_group"))
			.appendItems(stacks ->
					Registry.ITEM.stream()
							.filter(item -> item != Items.AIR)
							.map(ItemStack::new)
							.forEach(stacks::add)
			).build();

	public static final QuiltItemGroup ITEM_GROUP_WITH_TEXTURE_ICON = QuiltItemGroup.create(new Identifier(NAMESPACE, "test_texture_icon_group"));

	private static final QuiltItemGroup[] MANY_GROUPS = IntStream.range(0, 20).mapToObj(i -> QuiltItemGroup.builder(new Identifier(NAMESPACE, "many_group_" + i)).build()).toArray(QuiltItemGroup[]::new);

	private static final ItemGroup NOT_QUILT_ITEM_GROUP = QuiltItemGroup.register(index -> new ItemGroup(index, NAMESPACE + ".test_not_quilt_group") {
		@Override
		public ItemStack createIcon() {
			return new ItemStack(Items.PHANTOM_MEMBRANE);
		}
	});

	@Override
	public void onInitialize(ModContainer mod) {
		DELAYED_ITEM_GROUP.setIcon(Items.EMERALD);
	}
}
