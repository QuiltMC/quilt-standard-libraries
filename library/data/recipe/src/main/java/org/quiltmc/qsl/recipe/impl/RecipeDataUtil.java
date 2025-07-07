/*
 * Copyright 2025 The Quilt Project
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

package org.quiltmc.qsl.recipe.impl;

import static java.util.Objects.requireNonNull;

import java.util.Collection;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.CraftingCategory;

@ApiStatus.Internal
public final class RecipeDataUtil {
	public static final String DEFAULT_GROUP = "";
	public static final CraftingCategory DEFAULT_CRAFTING_CATEGORY = CraftingCategory.MISC;

	public static <T> T requireSpecified(T value, String name) {
		return requireNonNull(value, name + " must be specified");
	}

	public static ItemStack requireResult(ItemStack result) {
		requireSpecified(result, "result");
		if (result.isOf(Items.AIR)) {
			throw new IllegalArgumentException("result must not be air");
		}

		if (result.isEmpty()) {
			throw new IllegalArgumentException("result must not be empty: " + result);
		}

		return result;
	}

	public static <T, I extends Iterable<T>> I requireNonEmpty(I iterable, String name) {
		final boolean empty = iterable instanceof Collection<?> collection ? collection.isEmpty()
				: !requireSpecified(iterable, name).iterator().hasNext();

		if (empty) {
			throw new IllegalArgumentException(name + " must not be empty");
		}

		return iterable;
	}
}
