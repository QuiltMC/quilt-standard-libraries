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

package org.quiltmc.qsl.recipe.test;

import java.util.Arrays;
import java.util.List;
import java.util.Random;

import net.minecraft.block.PressurePlateBlock;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.display.SlotDisplay;
import net.minecraft.recipe.display.SlotDisplayContext;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.context.ContextMap;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.recipe.api.RecipeManagerHelper;
import org.quiltmc.qsl.recipe.api.data.ShapedRecipeData;
import org.quiltmc.qsl.recipe.api.data.ShapelessRecipeData;

public class RecipeTestMod implements ModInitializer {
	public static final String NAMESPACE = "quilt_recipe_testmod";
	private static final Random RANDOM = new Random();
	private static final List<Item> RANDOM_ITEMS_POOL = Arrays.asList(
			Items.COMMAND_BLOCK,
			Items.COMMAND_BLOCK_MINECART,
			Items.ELYTRA,
			Items.CHAIN_COMMAND_BLOCK,
			Items.REPEATING_COMMAND_BLOCK
	);

	@Override
	public void onInitialize(ModContainer mod) {
		// coal/charcoal -> diamond
		RecipeManagerHelper.registerStaticRecipe(
				Identifier.of(NAMESPACE, "test1"),
				ShapedRecipeData.builder()
					// It's important to test a tag ingredient in a static recipe
					// to make sure we don't resolve the tag early.
					.pattern("*")
					.ingredient('*', ItemTags.COALS)
					.result(new ItemStack(Items.DIAMOND))
					.build()
		);

		RecipeManagerHelper.addRecipes(handler -> {
			handler.register(
					Identifier.of(NAMESPACE, "test2"),
					id -> ShapedRecipeData.builder()
						.pattern(
							"IG",
							"C#"
						)
						.ingredient('I', Items.IRON_INGOT)
						.ingredient('G', Items.GOLD_INGOT)
						.ingredient('C', Items.COAL)
						.ingredient('#', Items.CHARCOAL)
						.result(pickRandomStack())
						.build()
			);
		});

		RecipeManagerHelper.modifyRecipes(handler -> {
			handler.replace(
					Identifier.ofDefault("acacia_button"),
					ShapelessRecipeData.builder()
						.ingredient(Items.ACACIA_PLANKS)
						.result(new ItemStack(Items.NETHER_STAR))
						.build()
			);

			handler.replace(
					Identifier.ofDefault("oak_button"),
					ShapedRecipeData.builder()
						.pattern(
							"A",
							"C"
						)
						.ingredient('A', ItemTags.PLANKS)
						.ingredient('C', Items.COAL)
						.result(new ItemStack(Items.NETHER_BRICK))
						.build()
			);
		});

		RecipeManagerHelper.removeRecipes(handler -> {
			handler.removeIf(RecipeType.CRAFTING, craftingRecipe -> {
				return craftingRecipe
					.value()
					.getDisplays()
					.stream()
					.flatMap(recipeDisplay -> recipeDisplay
						.result()
						.resolveItems(
							new ContextMap.Builder().build(SlotDisplayContext.CONTEXT),
							SlotDisplay.ItemStackMapper.INSTANCE
						)
					)
					.map(ItemStack::getItem)
					.anyMatch(item -> item instanceof BlockItem blockItem
						&& blockItem.getBlock() instanceof PressurePlateBlock);
			});
		});
	}

	private static ItemStack pickRandomStack() {
		Item item = RANDOM_ITEMS_POOL.get(RANDOM.nextInt(RANDOM_ITEMS_POOL.size()));
		return new ItemStack(item);
	}
}
