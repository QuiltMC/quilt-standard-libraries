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

package org.quiltmc.qsl.recipe.mixin;

import java.util.ArrayList;

import com.google.gson.JsonObject;
import it.unimi.dsi.fastutil.chars.Char2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2CharOpenHashMap;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.data.server.recipe.ShapedRecipeJsonFactory;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.collection.DefaultedList;

import org.quiltmc.qsl.recipe.api.serializer.QuiltRecipeSerializer;

@Mixin(ShapedRecipe.Serializer.class)
public abstract class ShapedRecipeSerializerMixin implements QuiltRecipeSerializer<ShapedRecipe> {
	@Override
	public JsonObject toJson(ShapedRecipe recipe) {
		DefaultedList<Ingredient> recipeIngredients = recipe.getIngredients();
		var ingredients = new Object2CharOpenHashMap<Ingredient>();
		var inputs = new Char2ObjectOpenHashMap<Ingredient>();
		ingredients.defaultReturnValue(' ');
		char currentChar = 'A';

		for (Ingredient ingredient : recipeIngredients) {
			if (!ingredient.isEmpty()
					&& ingredients.putIfAbsent(ingredient, currentChar) == ingredients.defaultReturnValue()) {
				inputs.putIfAbsent(currentChar, ingredient);
				currentChar++;
			}
		}

		var pattern = new ArrayList<String>();
		var patternLine = new StringBuilder();

		for (int i = 0; i < recipeIngredients.size(); i++) {
			if (i != 0 && i % recipe.getWidth() == 0) {
				pattern.add(patternLine.toString());
				patternLine.setLength(0);
			}

			Ingredient ingredient = recipeIngredients.get(i);
			patternLine.append(ingredients.getChar(ingredient));
		}

		pattern.add(patternLine.toString());

		var result = recipe.getResult(null);

		return new ShapedRecipeJsonFactory.ShapedRecipeJsonProvider(
				recipe.getId(),
				result.getItem(),
				result.getCount(),
				recipe.getGroup(),
				recipe.getCategory(),
				pattern,
				inputs,
				null, null,
				recipe.showNotification()
		).toJson();
	}
}
