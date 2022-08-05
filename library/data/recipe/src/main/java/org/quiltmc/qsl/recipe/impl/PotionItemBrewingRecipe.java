package org.quiltmc.qsl.recipe.impl;

import com.google.gson.JsonObject;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class PotionItemBrewingRecipe extends AbstractBrewingRecipe<Item> {
	public PotionItemBrewingRecipe(Identifier id, Item input, Ingredient ingredient, Item output, int fuel) {
		super(id, input, ingredient, output, fuel);
		this.ghostOutput = new ItemStack(this.output);
	}

	@Override
	protected ItemStack craft(int slot, ItemStack input) {
		return PotionUtil.setPotion(new ItemStack(this.output), PotionUtil.getPotion(input));
	}

	@Override
	public boolean matches(int slot, ItemStack input) {
		return input.isOf(this.input);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeImpl.POTION_ITEM_SERIALIZER;
	}

	static class Serializer extends AbstractBrewingSerializer<Item, PotionItemBrewingRecipe> {
		Serializer(RecipeFactory<Item, PotionItemBrewingRecipe> recipeFactory) {
			super(recipeFactory);
		}

		@Override
		public Item deserialize(String element, JsonObject json) {
			return JsonHelper.getItem(json, element);
		}

		@Override
		public Item deserialize(PacketByteBuf buf) {
			return buf.readById(Registry.ITEM);
		}

		@Override
		public void serialize(Item item, String element, JsonObject json) {
			json.addProperty(element, Registry.ITEM.getId(item).toString());
		}

		@Override
		public void serialize(Item item, PacketByteBuf buf) {
			buf.writeId(Registry.ITEM, item);
		}
	}
}
