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

package org.quiltmc.qsl.recipe.mixin.accessor;

import java.util.Map;

import com.google.common.collect.Multimap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeHolder;
import net.minecraft.recipe.RecipeMap;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.RegistryKey;

@Mixin(RecipeMap.class)
public interface RecipeMapAccessor {
	@Accessor("byType")
	Multimap<RecipeType<?>, RecipeHolder<?>> quilt$getByType();

	@Accessor("byKey")
	Map<RegistryKey<Recipe<?>>, RecipeHolder<?>> quilt$getByKey();

	@Invoker("<init>")
	static RecipeMap quilt$create(
			Multimap<RecipeType<?>, RecipeHolder<?>> byType,
			Map<RegistryKey<Recipe<?>>, RecipeHolder<?>> byKey
	) {
		throw new AssertionError("Dummy method called");
	}
}
