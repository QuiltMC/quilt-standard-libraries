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

import java.util.Collections;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

import org.quiltmc.qsl.recipe.impl.ImmutableMapBuilderUtil;
import org.quiltmc.qsl.recipe.impl.RecipeManagerImpl;

@Mixin(RecipeManager.class)
public class RecipeManagerMixin {
	@Shadow
	private Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes;

	@Shadow
	private Map<Identifier, Recipe<?>> recipeFlatMap;

	@Inject(
			method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V",
			at = @At(value = "INVOKE", target = "Ljava/util/Map;entrySet()Ljava/util/Set;", remap = false, ordinal = 0),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onReload(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler,
			CallbackInfo ci,
			Map<RecipeType<?>, ImmutableMap.Builder<Identifier, Recipe<?>>> builderMap,
			ImmutableMap.Builder<Identifier, Recipe<?>> globalRecipeMapBuilder) {
		RecipeManagerImpl.apply(map, builderMap, globalRecipeMapBuilder);
	}

	/**
	 * Synthetic method in {@link RecipeManager#apply(Map, ResourceManager, Profiler)} as an argument of {@code toImmutableMap}.
	 *
	 * @author The Quilt Project, LambdAurora
	 * @reason Replaces immutable maps for mutable maps instead.
	 */
	@Overwrite
	private static Map<Identifier, Recipe<?>> method_20703(Map.Entry<RecipeType<?>, ImmutableMap.Builder<Identifier, Recipe<?>>> entry) {
		// This is cursed. Do not look.
		return ImmutableMapBuilderUtil.specialBuild(entry.getValue());
	}

	@Redirect(
			method = "apply",
			at = @At(
					value = "INVOKE",
					target = "Lcom/google/common/collect/ImmutableMap$Builder;build()Lcom/google/common/collect/ImmutableMap;",
					remap = false
			)
	)
	private ImmutableMap<Identifier, Recipe<?>> onCreateGlobalRecipeMap(ImmutableMap.Builder<Identifier, Recipe<?>> globalRecipeMapBuilder) {
		return null; // The original method bounds us to return an immutable map, but we do not want that!
	}

	@Inject(
			method = "apply",
			at = @At(value = "INVOKE", target = "Lorg/slf4j/Logger;info(Ljava/lang/String;Ljava/lang/Object;)V", remap = false),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void onReloadEnd(Map<Identifier, JsonElement> map, ResourceManager resourceManager, Profiler profiler,
			CallbackInfo ci,
			Map<RecipeType<?>, ImmutableMap.Builder<Identifier, Recipe<?>>> builderMap,
			ImmutableMap.Builder<Identifier, Recipe<?>> globalRecipeMapBuilder) {
		Map<Identifier, Recipe<?>> globalRecipes = ImmutableMapBuilderUtil.specialBuild(globalRecipeMapBuilder);

		RecipeManagerImpl.applyModifications((RecipeManager) (Object) this, this.recipes, globalRecipes);

		this.recipeFlatMap = Collections.unmodifiableMap(globalRecipes);
	}
}
