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
import java.util.SortedMap;

import com.google.common.collect.Iterables;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeHolder;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeMap;
import net.minecraft.registry.HolderLookup;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.recipe.impl.RecipeManagerImpl;

@Mixin(RecipeManager.class)
abstract class RecipeManagerMixin {
	@Shadow
	@Final
	private HolderLookup.Provider registries;

	@ModifyArg(
			method = "prepare(Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)"
				+ "Lnet/minecraft/recipe/RecipeMap;",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/recipe/RecipeMap;create(Ljava/lang/Iterable;)Lnet/minecraft/recipe/RecipeMap;"
			)
	)
	private Iterable<RecipeHolder<?>> addRecipes(
			Iterable<RecipeHolder<?>> recipes,
			@Local SortedMap<Identifier, Recipe<?>> resourceMap
	) {
		ArrayList<RecipeHolder<?>> modifiableRecipes;
		if (recipes instanceof ArrayList<RecipeHolder<?>> arrayList) {
			modifiableRecipes = arrayList;
		} else {
			modifiableRecipes = new ArrayList<>();
			Iterables.addAll(modifiableRecipes, recipes);
		}

		modifiableRecipes.addAll(RecipeManagerImpl.addRecipes(resourceMap, this.registries));

		return modifiableRecipes;
	}

	@Inject(
			method = "apply(Lnet/minecraft/recipe/RecipeMap;Lnet/minecraft/resource/ResourceManager;"
				+ "Lnet/minecraft/util/profiler/Profiler;)V",
			at = @At("HEAD")
	)
	private void applyModifications(CallbackInfo ci, @Local(argsOnly = true) LocalRef<RecipeMap> recipeMap) {
		recipeMap.set(RecipeManagerImpl.applyModifications(
				(RecipeManager) (Object) this, recipeMap.get(), this.registries)
		);
	}
}
