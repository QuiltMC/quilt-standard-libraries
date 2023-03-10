/*
 * Copyright 2022-2023 QuiltMC
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

import net.minecraft.data.server.recipe.SmithingRecipeJsonFactory;
import net.minecraft.recipe.SmithingRecipe;

import org.quiltmc.qsl.recipe.api.serializer.QuiltRecipeSerializer;

@Mixin(SmithingRecipe.Serializer.class)
public abstract class SmithingRecipeSerializerMixin implements QuiltRecipeSerializer<SmithingRecipe> {
	@Override
	public JsonObject toJson(SmithingRecipe recipe) {
		var accessor = (SmithingRecipeAccessor) recipe;

		return new SmithingRecipeJsonFactory.SmithingRecipeJsonProvider(
				recipe.getId(),
				this,
				accessor.getBase(), accessor.getAddition(), recipe.getOutput().getItem(),
				null, null
		).toJson();
	}
}
