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

import java.util.function.Supplier;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;

/**
 * Extensions for the {@link ItemGroup} class. Currently, the only extension is setting the icon with either an {@link ItemConvertible} or {@link ItemStack} after the item has been created ({@link QuiltItemGroup#setIcon(ItemConvertible)}, {@link QuiltItemGroup#setIcon(ItemStack)}).
 */
public class QuiltItemGroup extends ItemGroup {
	private final @Nullable Supplier<ItemStack> iconSupplier;
	private @Nullable ItemStack icon;

	@ApiStatus.Internal
	protected QuiltItemGroup(int index, String id, @Nullable Supplier<ItemStack> iconSupplier) {
		super(index, id);
		this.iconSupplier = iconSupplier;
	}

	/**
	 * Sets the {@link Item} to use as the icon for the {@link ItemGroup}
	 * @param icon The {@link Item} for the icon
	 */
	public void setIcon(@NotNull ItemConvertible icon) {
		this.icon = new ItemStack(icon);
	}

	/**
	 * Sets the {@link ItemStack} to use as the icon for the {@link ItemGroup}, allowing for NBT to be used
	 * @param icon The {@link ItemStack} for the icon
	 */
	public void setIcon(@NotNull ItemStack icon) {
		this.icon = icon;
	}

	@Override
	public ItemStack createIcon() {
		if (iconSupplier != null && !iconSupplier.get().isEmpty()) {
			return iconSupplier.get();
		} else {
			if (icon == null) {
				return ItemStack.EMPTY;
			}
			return icon;
		}
	}
}
