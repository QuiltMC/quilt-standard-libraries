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

package org.quiltmc.qsl.itemgroup.api;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.quiltmc.qsl.itemgroup.impl.ItemGroupExtensions;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

/**
 * A builder class that helps with creating and adding {@link ItemGroup}s to the {@link CreativeInventoryScreen}.
 * <p>
 * Potential uses:
 * <p>
 * Creating an {@link ItemGroup} and then supplying the item icon:
 * <pre>{@code
 * public class MyMod implements ModInitializer {
 *   public static final QuiltItemGroup MY_ITEM_GROUP =
 *   	new QuiltItemGroupBuilder(
 *   		new Identifier("my_mod:item_group"))
 *   	.build();
 *
 *   public static Item MY_ITEM;
 *
 *   @Override
 *   public void onInitialize() {
 *     MY_ITEM = new Item(new QuiltItemSettings().group(MY_ITEM_GROUP));
 *     MY_ITEM_GROUP.icon(MY_ITEM);
 *   }
 * }
 * }</pre>
 * <p>
 * Creating an {@link ItemGroup} with the icon as a supplier:
 * <pre>{@code
 * public class MyMod implements ModInitializer {
 *   public static final QuiltItemGroup MY_ITEM_GROUP =
 *   	new QuiltItemGroupBuilder(
 *   		new Identifier("my_mod:item_group"))
 *   	.icon(() -> new ItemStack(MyMod.MY_ITEM))
 *   	.build();
 *
 *   public static Item MY_ITEM;
 *
 *   @Override
 *   public void onInitialize() {
 *     MY_ITEM = new Item(new QuiltItemSettings().group(MY_ITEM_GROUP));
 *   }
 * }
 * }</pre>
 */
public final class QuiltItemGroupBuilder {
	private final Identifier identifier;
	private Supplier<ItemStack> stackSupplier = () -> ItemStack.EMPTY;
	private Consumer<List<ItemStack>> stacksForDisplay;

	/**
	 * Create a new {@link QuiltItemGroupBuilder}. Using the constructor allows for the use of the {@link QuiltItemGroupBuilder#icon(Supplier)} and {@link QuiltItemGroupBuilder#appendItems(Consumer)} methods.
	 * Manually setting the icon with {@link QuiltItemGroup#setIcon(Item)} is possible after calling {@link QuiltItemGroupBuilder#build()}
	 *
	 * @param identifier the {@link Identifier} will become the name of the {@link ItemGroup} and will be used for the translation key
	 */
	public QuiltItemGroupBuilder(Identifier identifier) {
		this.identifier = identifier;
	}

	/**
	 * This is used to add an icon to the {@link ItemGroup}.
	 *
	 * @param stackSupplier the supplier should return the {@link ItemStack} that you wish to show on the tab
	 * @return {@code this}
	 */
	public QuiltItemGroupBuilder icon(Supplier<ItemStack> stackSupplier) {
		this.stackSupplier = stackSupplier;
		return this;
	}

	/**
	 * Adds a custom list of items to be displayed in a tab, such as items with enchantments, potions, or other NBT values.
	 *
	 * @param stacksForDisplay Add {@link ItemStack}s to this list to show in the {@link ItemGroup}
	 * @return {@code this}
	 */
	public QuiltItemGroupBuilder appendItems(Consumer<List<ItemStack>> stacksForDisplay) {
		this.stacksForDisplay = stacksForDisplay;
		return this;
	}

	/**
	 * This is a single method that makes creating an {@link ItemGroup} with an icon one call.
	 *
	 * @param identifier    the id will become the name of the {@link ItemGroup} and will be used for the translation key
	 * @param stackSupplier the supplier should return the {@link ItemStack} that you wish to show on the tab
	 * @return An instance of the created {@link ItemGroup}
	 */
	public static ItemGroup buildWithIcon(Identifier identifier, Supplier<ItemStack> stackSupplier) {
		return new QuiltItemGroupBuilder(identifier).icon(stackSupplier).build();
	}

	/**
	 * This is a single method that creates an {@link ItemGroup} one call. This method should only be used when setting the icon with {@link QuiltItemGroup#setIcon(Item)}.
	 *
	 * @param identifier    the id will become the name of the {@link ItemGroup} and will be used for the translation key
	 * @return An instance of the created {@link ItemGroup}
	 */
	public static QuiltItemGroup buildImmediate(Identifier identifier) {
		return new QuiltItemGroupBuilder(identifier).build();
	}

	/**
	 * @return An {@link ItemGroup} built from {@code this}
	 */
	public QuiltItemGroup build() {
		((ItemGroupExtensions) ItemGroup.GROUPS[0]).qsl$expandArray();
		return new QuiltItemGroup(ItemGroup.GROUPS.length - 1, String.format("%s.%s", identifier.getNamespace(), identifier.getPath()), stackSupplier) {
			@Override
			public void appendStacks(DefaultedList<ItemStack> stacks) {
				if (stacksForDisplay != null) {
					stacksForDisplay.accept(stacks);
					return;
				}

				super.appendStacks(stacks);
			}
		};
	}
}
