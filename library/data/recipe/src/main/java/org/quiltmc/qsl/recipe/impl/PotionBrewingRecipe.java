package org.quiltmc.qsl.recipe.impl;

import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

public class PotionBrewingRecipe extends AbstractBrewingRecipe<Potion> {
	public PotionBrewingRecipe(Identifier id, Potion input, Ingredient ingredient, Potion output, int fuel) {
		super(id, input, ingredient, output, fuel);
		PotionUtil.setPotion(this.ghostOutput, this.output);
	}

	@Override
	protected ItemStack craft(int slot, ItemStack input) {
		return PotionUtil.setPotion(input.copy(), this.output);
	}

	@Override
	public boolean matches(int slot, ItemStack input) {
		return !input.isEmpty() && PotionUtil.getPotion(input).equals(this.input);
	}

	@Override
	public RecipeSerializer<? extends PotionBrewingRecipe> getSerializer() {
		return RecipeImpl.POTION_SERIALIZER;
	}

	static class Serializer<R extends PotionBrewingRecipe> extends AbstractBrewingSerializer<Potion, R> {
		Serializer(RecipeFactory<Potion, R> recipeFactory) {
			super(recipeFactory);
		}

		@Override
		public Potion deserialize(String element, JsonObject json) {
			return Potion.byId(JsonHelper.getString(json, element, "empty"));
		}

		@Override
		public Potion deserialize(PacketByteBuf buf) {
			return Potion.byId(buf.readString());
		}

		@Override
		public void serialize(Potion value, String element, JsonObject json) {
			json.addProperty(element, Registry.POTION.getId(value).toString());
		}

		@Override
		public void serialize(Potion value, PacketByteBuf buf) {
			buf.writeString(Registry.POTION.getId(value).toString());
		}
	}
}
