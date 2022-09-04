package org.quiltmc.qsl.item.test;

import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.setting.api.QuiltItemSettings;

public class RecipeRemainderTests implements ModInitializer {
	// Static field so we can use it in BrewingRecipeRegistryMixin
	public static final Item POTION_INGREDIENT_REMAINDER = Registry.register(
			Registry.ITEM,
			new Identifier(QuiltItemSettingsTests.NAMESPACE, "potion_ingredient_remainder"),
			new Item(
					new QuiltItemSettings().group(ItemGroup.MISC).recipeRemainder(
							(original, recipe) -> new ItemStack(Items.BLAZE_POWDER)
					)
			)
	);

	@Override
	public void onInitialize(ModContainer mod) {
		Item hammerItem = new Item(new QuiltItemSettings().group(ItemGroup.TOOLS).maxDamage(16).damageIfUsedInCrafting());
		Registry.register(Registry.ITEM, new Identifier(QuiltItemSettingsTests.NAMESPACE, "hammer"), hammerItem);

		Item furnaceInputRemainder = new Item(new QuiltItemSettings().group(ItemGroup.MISC).recipeRemainder((original, recipe) -> {
			if (recipe != null && recipe.getType() == RecipeType.SMELTING) {
				return Items.DIAMOND.getDefaultStack();
			}
			return ItemStack.EMPTY;
		}));
		Registry.register(Registry.ITEM, new Identifier(QuiltItemSettingsTests.NAMESPACE, "weird_ore"), furnaceInputRemainder);

		Item furnaceInputSelfRemainder = new Item(new QuiltItemSettings().group(ItemGroup.MISC).recipeRemainder((original, recipe) -> {
			if (recipe != null && recipe.getType() == RecipeType.SMELTING) {
				var remainder = original.copy();
				remainder.setCount(2);
				return remainder;
			}
			return ItemStack.EMPTY;
		}));
		Registry.register(Registry.ITEM, new Identifier(QuiltItemSettingsTests.NAMESPACE, "infinite_ore"), furnaceInputSelfRemainder);

		Item furnaceFuelSelfRemainder = new Item(new QuiltItemSettings().group(ItemGroup.MISC).recipeRemainder((original, recipe) -> {
			var remainder = original.copy();
			if (recipe != null) {
				if (recipe.getType() == RecipeType.SMELTING) {
					remainder.setCount(1);
				} else if (recipe.getType() == RecipeType.SMOKING) {
					remainder.setCount(2);
				} else if (recipe.getType() == RecipeType.BLASTING) {
					remainder.setCount(3);
				}
				return remainder;
			}
			return ItemStack.EMPTY;
		}));
		Registry.register(Registry.ITEM, new Identifier(QuiltItemSettingsTests.NAMESPACE, "infinite_fuel"), furnaceFuelSelfRemainder);

		Item smithingInputRemainder = new Item(new QuiltItemSettings().group(ItemGroup.MISC).recipeRemainder((original, recipe) -> {
			if (recipe != null && recipe.getType() == RecipeType.SMITHING) {
				return Items.NETHERITE_INGOT.getDefaultStack();
			}
			return Items.NETHERITE_SCRAP.getDefaultStack();
		}));
		Registry.register(Registry.ITEM, new Identifier(QuiltItemSettingsTests.NAMESPACE, "infinite_netherite"), smithingInputRemainder);

		Item loomInputRemainder = new DyeItem(DyeColor.RED, new QuiltItemSettings().group(ItemGroup.MISC).maxDamage(100).damageIfUsedInCrafting());
		Registry.register(Registry.ITEM, new Identifier(QuiltItemSettingsTests.NAMESPACE, "infinite_dye"), loomInputRemainder);

		Item cuttingInputRemainder = new Item(new QuiltItemSettings().group(ItemGroup.MISC).recipeRemainder((original, recipe) -> {
			if (recipe != null && recipe.getType() == RecipeType.STONECUTTING) {
				return Items.STONE.getDefaultStack();
			}
			return ItemStack.EMPTY;
		}));
		Registry.register(Registry.ITEM, new Identifier(QuiltItemSettingsTests.NAMESPACE, "infinite_stone"), cuttingInputRemainder);
	}
}
