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

package org.quiltmc.qsl.recipe.api.builder;

import java.util.Objects;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Recipe;
import net.minecraft.util.Identifier;

/**
 * Represents the basis of a recipe builder.
 *
 * @param <SELF>   the type of the recipe builder
 * @param <RESULT> the type of the recipe
 */
public abstract class RecipeBuilder<SELF extends RecipeBuilder<SELF, RESULT>, RESULT extends Recipe<?>> {
	protected ItemStack output;

	/**
	 * Sets the output of the shaped crafting recipe.
	 *
	 * @param stack the output item stack.
	 * @return this builder
	 */
	@SuppressWarnings("unchecked")
	public SELF output(ItemStack stack) {
		this.output = stack;
		return (SELF) this;
	}

	protected void checkOutputItem() {
		Objects.requireNonNull(this.output, "The output stack cannot be null.");
	}

	/**
	 * Builds the recipe.
	 *
	 * @param id    the identifier of the recipe
	 * @param group the group of the recipe
	 * @return the shaped recipe
	 */
	public abstract RESULT build(Identifier id, String group);
}
