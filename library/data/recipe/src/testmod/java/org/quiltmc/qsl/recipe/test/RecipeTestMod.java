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
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.recipe.api.RecipeManagerHelper;
import org.quiltmc.qsl.recipe.api.builder.QuiltRecipeBuilders;
import org.quiltmc.qsl.recipe.api.builder.VanillaRecipeBuilders;

public class RecipeTestMod implements ModInitializer {
	public static final RecipeSerializer<SwordImbuingTestRecipe> TEST_SERIALIZER = new SwordImbuingTestRecipe.SwordImbuingTestSerializer(SwordImbuingTestRecipe::new);
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
		Registry.register(Registries.RECIPE_SERIALIZER, new Identifier(NAMESPACE, "sword_imbuing"), TEST_SERIALIZER);

		// Recipe with stick -> diamond
		RecipeManagerHelper.registerStaticRecipe(
				VanillaRecipeBuilders.shapelessRecipe(new ItemStack(Items.DIAMOND))
						.ingredient(Items.STICK)
						.build(new Identifier(NAMESPACE, "test1"), ""));

		// Recipe with lingering potion -> splash potion
		RecipeManagerHelper.registerStaticRecipe(
				QuiltRecipeBuilders.potionItemRecipe(Items.LINGERING_POTION, Items.SPLASH_POTION)
						.fuel(2)
						.brewTime(40)
						.ingredient(Items.SPONGE)
						.build(new Identifier(NAMESPACE, "brew_item_test"), ""));

		// Recipe with water potion -> awkward potion
		RecipeManagerHelper.registerStaticRecipe(
				QuiltRecipeBuilders.potionRecipe(Potions.WATER, Potions.AWKWARD)
						.brewTime(10)
						.ingredient(Items.SOUL_SAND)
						.build(new Identifier(NAMESPACE, "brew_potion_test"), ""));

		// Recipe with water potion -> strength potion with added instant healing
		RecipeManagerHelper.registerStaticRecipe(
				QuiltRecipeBuilders.customPotionRecipe(Potions.WATER, Potions.STRENGTH)
						.brewTime(20)
						.ingredient(Items.EGG)
						.effects(new StatusEffectInstance(StatusEffects.INSTANT_HEALTH))
						.build(new Identifier(NAMESPACE, "brew_custom_potion_test"), ""));

		RecipeManagerHelper.addRecipes(handler -> {
			handler.register(new Identifier(NAMESPACE, "test2"),
					id -> VanillaRecipeBuilders.shapedRecipe("IG", "C#")
							.ingredient('I', Items.IRON_INGOT)
							.ingredient('G', Items.GOLD_INGOT)
							.ingredient('C', Items.COAL)
							.ingredient('#', Items.CHARCOAL)
							.output(pickRandomStack())
							.build(id, ""));
		});

		RecipeManagerHelper.modifyRecipes(handler -> {
			handler.replace(VanillaRecipeBuilders.shapelessRecipe(new ItemStack(Items.NETHER_STAR))
					.ingredient(Items.ACACIA_PLANKS)
					.build(new Identifier("acacia_button"), ""));
			handler.replace(VanillaRecipeBuilders.shapedRecipe("A", "C")
					.ingredient('A', ItemTags.PLANKS)
					.ingredient('C', Items.COAL)
					.output(new ItemStack(Items.NETHER_BRICK))
					.build(new Identifier("oak_button"), ""));
		});

		RecipeManagerHelper.removeRecipes(handler -> {
			handler.removeIf(RecipeType.CRAFTING, craftingRecipe -> {
				return craftingRecipe.getResult(handler.getRegistryManager()).getItem() instanceof BlockItem blockItem
						&& blockItem.getBlock() instanceof PressurePlateBlock;
			});
		});
	}

	private static ItemStack pickRandomStack() {
		Item item = RANDOM_ITEMS_POOL.get(RANDOM.nextInt(RANDOM_ITEMS_POOL.size()));
		return new ItemStack(item);
	}
}
