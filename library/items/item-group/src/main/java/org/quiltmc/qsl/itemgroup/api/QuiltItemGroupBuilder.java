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

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.itemgroup.impl.ItemGroupExtensions;

public final class QuiltItemGroupBuilder {
	private Identifier identifier;
	private Supplier<ItemStack> stackSupplier = () -> ItemStack.EMPTY;
	private Consumer<List<ItemStack>> stacksForDisplay;

	private QuiltItemGroupBuilder(Identifier identifier) {
		this.identifier = identifier;
	}

	/**
	 * Create a new {@link QuiltItemGroupBuilder}.
	 *
	 * @param identifier the {@link Identifier} will become the name of the {@link ItemGroup} and will be used for the translation key
	 * @return a {@link QuiltItemGroupBuilder}
	 */
	public static QuiltItemGroupBuilder create(Identifier identifier) {
		return new QuiltItemGroupBuilder(identifier);
	}

	/**
	 * This is used to add an icon to the {@link ItemGroup}.
	 *
	 * @param stackSupplier the supplier should return the {@link ItemStack} that you wish to show on the tab
	 * @return this
	 */
	public QuiltItemGroupBuilder icon(Supplier<ItemStack> stackSupplier) {
		this.stackSupplier = stackSupplier;
		return this;
	}

	/**
	 * This allows for a custom list of items to be displayed in a tab, this enabled tabs to be created with a custom set of items.
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
	 * @return An instance of the built {@link ItemGroup}
	 */
	public static ItemGroup build(Identifier identifier, Supplier<ItemStack> stackSupplier) {
		return new QuiltItemGroupBuilder(identifier).icon(stackSupplier).build();
	}

	/**
	 * @return An {@link ItemGroup} built from {@code this}
	 */
	public ItemGroup build() {
		((ItemGroupExtensions) ItemGroup.GROUPS[0]).expandArray();
		return new ItemGroup(ItemGroup.GROUPS.length - 1, String.format("%s.%s", identifier.getNamespace(), identifier.getPath())) {
			@Override
			public ItemStack createIcon() {
				return stackSupplier.get();
			}

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
