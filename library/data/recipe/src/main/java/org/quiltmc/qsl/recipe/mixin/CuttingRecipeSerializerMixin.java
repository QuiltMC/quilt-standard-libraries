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

import com.google.gson.JsonObject;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.data.server.recipe.SingleItemRecipeJsonFactory;
import net.minecraft.recipe.CuttingRecipe;

import org.quiltmc.qsl.recipe.api.serializer.QuiltRecipeSerializer;

@Mixin(CuttingRecipe.Serializer.class)
public abstract class CuttingRecipeSerializerMixin<T extends CuttingRecipe> implements QuiltRecipeSerializer<T> {
	@Override
	public JsonObject toJson(T recipe) {
		var result = recipe.getResult(null);

		return new SingleItemRecipeJsonFactory.SingleItemRecipeJsonProvider(recipe.getId(), this, recipe.getGroup(),
				recipe.getIngredients().get(0), result.getItem(), result.getCount(),
				null, null)
				.toJson();
	}
}
