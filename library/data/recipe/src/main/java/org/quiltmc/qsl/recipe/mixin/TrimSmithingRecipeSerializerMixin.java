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

import net.minecraft.data.server.recipe.TrimSmithingRecipeJsonFactory;
import net.minecraft.recipe.TrimSmithingRecipe;

import org.quiltmc.qsl.recipe.api.serializer.QuiltRecipeSerializer;

@Mixin(TrimSmithingRecipe.Serializer.class)
public abstract class TrimSmithingRecipeSerializerMixin implements QuiltRecipeSerializer<TrimSmithingRecipe> {
	@Override
	public JsonObject toJson(TrimSmithingRecipe recipe) {
		var accessor = (TrimSmithingRecipeAccessor) recipe;

		return new TrimSmithingRecipeJsonFactory.TrimSmithingRecipeJsonProvider(
				recipe.getId(),
				this,
				accessor.getTemplate(), accessor.getBase(), accessor.getAddition(),
				null,
				null
		).toJson();
	}
}
