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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.itemgroup.impl.ItemGroupExtensions;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

/**
 * Extensions for the {@link ItemGroup} class. Currently, the only extension is setting the icon with either an {@link ItemConvertible} or {@link ItemStack} after the item has been created ({@link QuiltItemGroup#setIcon(ItemConvertible)}, {@link QuiltItemGroup#setIcon(ItemStack)}).
 */
public final class QuiltItemGroup extends ItemGroup {
	private final @NotNull Supplier<ItemStack> iconSupplier;
	private @Nullable ItemStack icon;
	private final @Nullable Consumer<List<ItemStack>> stacksForDisplay;

	private QuiltItemGroup(int index, String id, Supplier<ItemStack> iconSupplier, @Nullable Consumer<List<ItemStack>> stacksForDisplay) {
		super(index, id);
		this.iconSupplier = iconSupplier;
		this.stacksForDisplay = stacksForDisplay;
	}

	/**
	 * Sets the {@link Item} to use as the icon for the {@link ItemGroup}
	 *
	 * @param icon The {@link Item} for the icon
	 */
	public void setIcon(ItemConvertible icon) {
		this.icon = new ItemStack(icon);
	}

	/**
	 * Sets the {@link ItemStack} to use as the icon for the {@link ItemGroup}, allowing for NBT to be used
	 *
	 * @param icon The {@link ItemStack} for the icon
	 */
	public void setIcon(ItemStack icon) {
		this.icon = icon;
	}

	@Override
	public ItemStack createIcon() {
		ItemStack supplierIcon = this.iconSupplier.get();
		if (!supplierIcon.isEmpty()) {
			return supplierIcon;
		} else if (icon == null) {
			return ItemStack.EMPTY;
		}

		return icon;
	}

	@Override
	public void appendStacks(DefaultedList<ItemStack> stacks) {
		super.appendStacks(stacks);

		if (stacksForDisplay != null) {
			stacksForDisplay.accept(stacks);
		}
	}

	/**
	 * Create a new {@link Builder}. Using the constructor allows for the use of the {@link Builder#icon(Supplier)} and {@link Builder#appendItems(Consumer)} methods.
	 * Manually setting the icon with {@link QuiltItemGroup#setIcon(ItemConvertible)} is possible after calling {@link Builder#build()}
	 *
	 * @param identifier the {@link Identifier} will become the name of the {@link ItemGroup} and will be used for the translation key
	 */
	public static Builder builder(Identifier identifier) {
		return new Builder(identifier);
	}

	/**
	 * This is a single method that creates an {@link ItemGroup} one call. This method should only be used when setting the icon with {@link QuiltItemGroup#setIcon(ItemConvertible)}.
	 *
	 * @param identifier the id will become the name of the {@link ItemGroup} and will be used for the translation key
	 * @return An instance of the created {@link ItemGroup}
	 */
	public static QuiltItemGroup create(Identifier identifier) {
		return new Builder(identifier).build();
	}

	/**
	 * This is a single method that makes creating an {@link ItemGroup} with an icon one call.
	 *
	 * @param identifier    the id will become the name of the {@link ItemGroup} and will be used for the translation key
	 * @param iconSupplier the supplier should return the {@link ItemStack} that you wish to show on the tab
	 * @return An instance of the created {@link QuiltItemGroup}
	 */
	public static QuiltItemGroup createWithIcon(Identifier identifier, @NotNull Supplier<ItemStack> iconSupplier) {
		return new Builder(identifier).icon(iconSupplier).build();
	}

	/**
	 * A builder class that helps with creating and adding {@link ItemGroup}s to the {@link CreativeInventoryScreen}.
	 * <p>
	 * Potential uses:
	 * <p>
	 * Creating an {@link ItemGroup} and then supplying the item icon:
	 * <pre>{@code
	 * public class MyMod implements ModInitializer {
	 *   public static final QuiltItemGroup MY_ITEM_GROUP =
	 *   	QuiltItemGroup.builder(
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
	 *   	QuiltItemGroup.builder(
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
	public static final class Builder {
		private final Identifier identifier;
		private Supplier<ItemStack> iconSupplier = () -> ItemStack.EMPTY;
		private Consumer<List<ItemStack>> stacksForDisplay;

		private Builder(Identifier identifier) {
			this.identifier = identifier;
		}

		/**
		 * This is used to add an icon to the {@link ItemGroup}.
		 *
		 * @param iconSupplier the supplier should return the {@link ItemStack} that you wish to show on the tab
		 * @return {@code this}
		 */
		public Builder icon(@NotNull Supplier<ItemStack> iconSupplier) {
			this.iconSupplier = iconSupplier;
			return this;
		}

		/**
		 * Adds a custom list of items to be displayed in a tab, such as items with enchantments, potions, or other NBT values.
		 *
		 * @param stacksForDisplay Add {@link ItemStack}s to this list to show in the {@link ItemGroup}
		 * @return {@code this}
		 */
		public Builder appendItems(@Nullable Consumer<List<ItemStack>> stacksForDisplay) {
			this.stacksForDisplay = stacksForDisplay;
			return this;
		}

		/**
		 * @return A new {@link QuiltItemGroup}
		 */
		public QuiltItemGroup build() {
			((ItemGroupExtensions) GROUPS[0]).qsl$expandArray();
			return new QuiltItemGroup(GROUPS.length - 1, String.format("%s.%s", identifier.getNamespace(), identifier.getPath()), iconSupplier, stacksForDisplay);
		}
	}
}
