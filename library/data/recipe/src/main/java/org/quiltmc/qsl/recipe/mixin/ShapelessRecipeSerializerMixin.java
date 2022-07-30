/*
 * Copyright 2022 QuiltMC
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

import net.minecraft.data.server.recipe.ShapelessRecipeJsonFactory;
import net.minecraft.recipe.ShapelessRecipe;

import org.quiltmc.qsl.recipe.api.serializer.QuiltRecipeSerializer;

@Mixin(ShapelessRecipe.Serializer.class)
public abstract class ShapelessRecipeSerializerMixin implements QuiltRecipeSerializer<ShapelessRecipe> {
	@Override
	public JsonObject toJson(ShapelessRecipe recipe) {
		return new ShapelessRecipeJsonFactory.ShapelessRecipeJsonProvider(recipe.getId(),
				recipe.getOutput().getItem(), recipe.getOutput().getCount(),
				recipe.getGroup(), recipe.getIngredients(), null, null)
				.toJson();
	}
}
