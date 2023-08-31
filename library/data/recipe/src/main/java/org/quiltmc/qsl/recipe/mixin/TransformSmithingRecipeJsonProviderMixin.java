/*
 * Copyright 2023 The Quilt Project
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

import net.minecraft.data.server.recipe.TransformSmithingRecipeJsonFactory;
import net.minecraft.recipe.TransformSmithingRecipe;

import org.quiltmc.qsl.recipe.api.serializer.QuiltRecipeSerializer;

@Mixin(TransformSmithingRecipe.Serializer.class)
public abstract class TransformSmithingRecipeJsonProviderMixin implements QuiltRecipeSerializer<TransformSmithingRecipe> {
	@Override
	public JsonObject toJson(TransformSmithingRecipe recipe) {
		var accessor = (TransformSmithingRecipeAccessor) recipe;
		return new TransformSmithingRecipeJsonFactory.TransformSmithingRecipeJsonProvider(
				recipe.getId(),
				this,
				accessor.getTemplate(), accessor.getBase(), accessor.getAddition(),
				recipe.getResult(null).getItem(),
				null, null
		).toJson();
	}
}
