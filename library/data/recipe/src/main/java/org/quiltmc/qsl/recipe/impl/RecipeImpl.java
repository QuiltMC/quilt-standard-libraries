package org.quiltmc.qsl.recipe.impl;

import net.minecraft.item.Item;
import net.minecraft.recipe.RecipeType;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class RecipeImpl implements ModInitializer {
	public static final Identifier BREWING_ID = new Identifier("quilt_recipe", "brewing");
	public static final RecipeType<AbstractBrewingRecipe<?>> BREWING = RecipeType.register(RecipeImpl.BREWING_ID.toString());
	static final PotionBrewingRecipe.Serializer<PotionBrewingRecipe> POTION_SERIALIZER = new PotionBrewingRecipe.Serializer<>(PotionBrewingRecipe::new);
	static final CustomPotionBrewingRecipe.Serializer CUSTOM_POTION_SERIALIZER = new CustomPotionBrewingRecipe.Serializer(CustomPotionBrewingRecipe::new);
	static final PotionItemBrewingRecipe.Serializer POTION_ITEM_SERIALIZER = new PotionItemBrewingRecipe.Serializer(PotionItemBrewingRecipe::new);
	public static final TagKey<Item> POTIONS = TagKey.of(Registry.ITEM_KEY, new Identifier("quilt", "potions"));

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registry.RECIPE_SERIALIZER, new Identifier("quilt_recipe", "potion_brewing"), POTION_SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, new Identifier("quilt_recipe", "custom_potion_brewing"), CUSTOM_POTION_SERIALIZER);
		Registry.register(Registry.RECIPE_SERIALIZER, new Identifier("quilt_recipe", "potion_item_brewing"), POTION_ITEM_SERIALIZER);
	}
}
