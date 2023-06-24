/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.item.group.api;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;

import org.quiltmc.qsl.item.group.impl.ItemGroupExtensions;

/**
 * Extensions for the {@link ItemGroup} class.
 * Currently, the only extension is setting the icon with either an {@link ItemConvertible} or {@link ItemStack}
 * after the item has been created ({@link QuiltItemGroup#setIcon(ItemConvertible)}, {@link QuiltItemGroup#setIcon(ItemStack)}).
 * <p>
 * A {@link QuiltItemGroup} can be directly created with {@link QuiltItemGroup#create(Identifier)} or {@link QuiltItemGroup#createWithIcon(Identifier, Supplier)}.<br>
 * A {@link Builder}, which is used to add specific {@link ItemStack}s, especially with {@link net.minecraft.nbt.NbtElement}s, can be obtained with {@link QuiltItemGroup#builder(Identifier)}.
 */
public final class QuiltItemGroup extends ItemGroup {
	private final Supplier<ItemStack> iconSupplier;
	private @Nullable ItemStack icon;
	private final @Nullable Consumer<List<ItemStack>> stacksForDisplay;
	private final @Nullable Text displayText;

	private QuiltItemGroup(int index, String id, Supplier<ItemStack> iconSupplier, @Nullable Consumer<List<ItemStack>> stacksForDisplay, @Nullable Text displayText) {
		super(index, id);
		this.iconSupplier = iconSupplier;
		this.stacksForDisplay = stacksForDisplay;
		this.displayText = displayText;
	}

	/**
	 * Sets the {@link Item} to use as the icon for the {@link ItemGroup}
	 *
	 * @param icon the {@link Item} for the icon
	 */
	public void setIcon(ItemConvertible icon) {
		this.icon = icon.asItem().getDefaultStack();
	}

	/**
	 * Sets the {@link ItemStack} to use as the icon for the {@link ItemGroup}, allowing for NBT to be used
	 *
	 * @param icon the {@link ItemStack} for the icon
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

		if (this.stacksForDisplay != null) {
			this.stacksForDisplay.accept(stacks);
		}
	}

	@Override
	public Text getTranslationKey() {
		return this.displayText == null ? super.getTranslationKey() : this.displayText;
	}

	/**
	 * Create a new {@link Builder}.
	 * Using the constructor allows for the use of the {@link Builder#icon(Supplier)} and {@link Builder#appendItems(Consumer)} methods.
	 * Manually setting the icon with {@link QuiltItemGroup#setIcon(ItemConvertible)} is possible after calling {@link Builder#build()}
	 *
	 * @param identifier the {@link Identifier} will become the name of the {@link ItemGroup} and will be used for the translation key
	 */
	public static Builder builder(Identifier identifier) {
		return new Builder(identifier);
	}

	/**
	 * This is a single method that creates an {@link ItemGroup} one call.
	 * This method should only be used when setting the icon with {@link QuiltItemGroup#setIcon(ItemConvertible)}.
	 *
	 * @param identifier the identifier will become the name of the {@link ItemGroup} and will be used for the translation key
	 * @return an instance of the created {@link ItemGroup}
	 */
	public static QuiltItemGroup create(Identifier identifier) {
		return new Builder(identifier).build();
	}

	/**
	 * This is a single method that makes creating an {@link ItemGroup} with an icon one call.
	 *
	 * @param identifier   the identifier will become the name of the {@link ItemGroup} and will be used for the translation key
	 * @param iconSupplier the supplier should return the {@link ItemStack} that you wish to show on the tab
	 * @return an instance of the created {@link QuiltItemGroup}
	 */
	public static QuiltItemGroup createWithIcon(Identifier identifier, @NotNull Supplier<ItemStack> iconSupplier) {
		return new Builder(identifier).icon(iconSupplier).build();
	}

	/**
	 * Appends a new {@link ItemGroup} to the end of the vanilla list of {@link ItemGroup}s.
	 *
	 * @param itemGroupFactory a factory that produces a new {@link ItemGroup} from an index
	 * @return the inserted {@link ItemGroup}
	 */
	public static <T extends ItemGroup> T register(Function<Integer, T> itemGroupFactory) {
		((ItemGroupExtensions) GROUPS[0]).quilt$expandArray();
		return itemGroupFactory.apply(GROUPS.length - 1);
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
	 * <p>
	 * Creating an {@link ItemGroup} with the icon as a supplier and custom {@link ItemStack}s:
	 * <pre>{@code
	 * public class MyMod implements ModInitializer {
	 *   public static final QuiltItemGroup MY_ITEM_GROUP =
	 *   	QuiltItemGroup.builder(
	 *   		new Identifier("my_mod:item_group"))
	 *   	.icon(() -> new ItemStack(MyMod.MY_ITEM))
	 *   	.appendItems(stacks -> {
	 *   	    stacks.add(new ItemStack(MyMod.MY_ITEM));
	 *   	    stacks.add(new ItemStack(Items.STONE));
	 *    })
	 *   	.build();
	 *
	 *   public static Item MY_ITEM;
	 *
	 *   @Override
	 *   public void onInitialize() {
	 *     MY_ITEM = new Item(new QuiltItemSettings());
	 *   }
	 * }
	 * }</pre>
	 */
	public static final class Builder {
		private final Identifier identifier;
		private Supplier<ItemStack> iconSupplier = () -> ItemStack.EMPTY;
		private Consumer<List<ItemStack>> stacksForDisplay;
		private Text text;

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
		 * @param stacksForDisplay add {@link ItemStack}s to this list to show in the {@link ItemGroup}
		 * @return {@code this}
		 */
		public Builder appendItems(@Nullable Consumer<List<ItemStack>> stacksForDisplay) {
			this.stacksForDisplay = stacksForDisplay;
			return this;
		}

		/**
		 * Set the {@link Text} used as the name when rendering the {@link ItemGroup}
		 *
		 * @param text The {@link Text} to render as the name
		 * @return {@code this}
		 */
		public Builder displayText(@NotNull Text text) {
			this.text = text;
			return this;
		}

		/**
		 * @return a new {@link QuiltItemGroup}
		 */
		public QuiltItemGroup build() {
			return register(index -> new QuiltItemGroup(
					index, String.format("%s.%s", this.identifier.getNamespace(), this.identifier.getPath()),
					this.iconSupplier, this.stacksForDisplay, this.text
			));
		}
	}
}
