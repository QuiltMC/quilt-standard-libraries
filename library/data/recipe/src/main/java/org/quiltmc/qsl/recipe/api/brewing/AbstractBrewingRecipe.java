/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.recipe.api.brewing;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.Potion;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

import org.quiltmc.qsl.recipe.api.Recipes;
import org.quiltmc.qsl.recipe.api.serializer.QuiltRecipeSerializer;

/**
 * The base for all Quilt brewing recipes.
 *
 * @param <T> what type the input and output represents.
 *           Vanilla would be {@link Potion} and {@link Item}
 * @see PotionBrewingRecipe
 * @see CustomPotionBrewingRecipe
 * @see PotionItemBrewingRecipe
 */
public abstract class AbstractBrewingRecipe<T> implements Recipe<BrewingStandBlockEntity> {
	public static final List<Ingredient> VALID_INGREDIENTS = new ArrayList<>();
	protected final T input;
	protected final Ingredient ingredient;
	protected final T output;
	protected final int fuel;
	protected final int brewTime;
	protected ItemStack ghostOutput;
	protected final String group;
	private final Identifier id;

	public AbstractBrewingRecipe(Identifier id, String group, T input, Ingredient ingredient, T output, int fuel, int brewTime) {
		this.id = id;
		this.group = group;
		this.input = input;
		this.ingredient = ingredient;
		VALID_INGREDIENTS.add(ingredient);
		this.output = output;
		this.ghostOutput = new ItemStack(Items.POTION);
		this.fuel = fuel;
		this.brewTime = brewTime;
	}

	@Override
	public RecipeType<AbstractBrewingRecipe<?>> getType() {
		return Recipes.BREWING;
	}

	@Override
	public ItemStack craft(BrewingStandBlockEntity inventory) {
		for (int i = 0; i < 3; i++) {
			if (this.matches(i, inventory.getStack(i))) {
				inventory.setStack(i, this.craft(i, inventory.getStack(i)));
			}
		}

		return ItemStack.EMPTY;
	}

	/**
	 * Transforms the input {@link ItemStack} to the output {@link ItemStack}.
	 * <p>
	 * The input is guaranteed to match the required input, as defined by {@link #matches(int, ItemStack)}.
	 *
	 * @param slot the index of the slot
	 * @param input the {@link ItemStack} in the provided slot
	 * @return the output {@link ItemStack}
	 */
	protected abstract ItemStack craft(int slot, ItemStack input);

	@Override
	public boolean matches(BrewingStandBlockEntity inventory, World world) {
		ItemStack ingredient = inventory.getStack(3);
		if (this.ingredient.test(ingredient)) {
			for (int i = 0; i < 3; ++i) {
				if (matches(i, inventory.getStack(i))) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Matches based on stacks in the potion slots.
	 *
	 * @param slot the index of the slot
	 * @param input the {@link ItemStack} in the provided slot
	 * @return {@code true} if the {@link ItemStack} is a valid {@link AbstractBrewingRecipe#input} for this recipe, or {@code false}
	 */
	public abstract boolean matches(int slot, ItemStack input);

	/**
	 * {@return how much fuel this recipe takes to craft}
	 */
	public int getFuelUse() {
		return this.fuel;
	}

	/**
	 * {@return how long this recipe takes to craft}
	 */
	public int getBrewTime() {
		return this.brewTime;
	}

	@Override
	public boolean fits(int width, int height) {
		return true;
	}

	@Override
	public DefaultedList<ItemStack> getRemainder(BrewingStandBlockEntity inventory) {
		// TODO integrate with custom recipe remainders
		return Recipe.super.getRemainder(inventory);
	}

	@Override
	public boolean isIgnoredInRecipeBook() {
		return true;
	}

	@Override
	public ItemStack createIcon() {
		return new ItemStack(Blocks.BREWING_STAND);
	}

	@Override
	public ItemStack getOutput() {
		return this.ghostOutput;
	}

	@Override
	public Identifier getId() {
		return this.id;
	}

	/**
	 * The base serializer for all Quilt brewing recipe serializers to extend.
	 *
	 * @param <T> the type of the recipe's input and output
	 * @param <R> the recipe
	 */
	public static abstract class AbstractBrewingSerializer<T, R extends AbstractBrewingRecipe<T>> implements RecipeSerializer<R>, QuiltRecipeSerializer<R> {
		private final RecipeFactory<T, ? extends R> recipeFactory;

		protected AbstractBrewingSerializer(RecipeFactory<T, ? extends R> recipeFactory) {
			this.recipeFactory = recipeFactory;
		}

		@Override
		public R read(Identifier id, JsonObject json) {
			String group = JsonHelper.getString(json, "group", "");
			Ingredient ingredient = Ingredient.fromJson(JsonHelper.getObject(json, "ingredient"));
			T input = this.deserialize("input", json);
			T output = this.deserialize("output", json);
			int fuel = JsonHelper.getInt(json, "fuel", 1);
			int brewTime = JsonHelper.getInt(json, "time", 400);
			return this.recipeFactory.create(id, group, input, ingredient, output, fuel, brewTime);
		}

		@Override
		public R read(Identifier id, PacketByteBuf buf) {
			String group = buf.readString();
			Ingredient ingredient = Ingredient.fromPacket(buf);
			T input = this.deserialize(buf);
			T output = this.deserialize(buf);
			int fuel = buf.readInt();
			int brewTime = buf.readInt();
			return this.recipeFactory.create(id, group, input, ingredient, output, fuel, brewTime);
		}

		@Override
		public void write(PacketByteBuf buf, R recipe) {
			buf.writeString(recipe.group);
			recipe.ingredient.write(buf);
			this.serialize(recipe.input, buf);
			this.serialize(recipe.output, buf);
			buf.writeInt(recipe.fuel);
			buf.writeInt(recipe.brewTime);
		}

		@Override
		public JsonObject toJson(R recipe) {
			JsonObject json = new JsonObject();
			json.addProperty("group", recipe.group);
			json.add("ingredient", recipe.ingredient.toJson());
			json.addProperty("type", Registry.RECIPE_SERIALIZER.getId(recipe.getSerializer()).toString());
			this.serialize(recipe.input, "input", json);
			this.serialize(recipe.output, "output", json);
			json.addProperty("fuel", recipe.fuel);
			json.addProperty("time", recipe.brewTime);
			return json;
		}

		/**
		 * Deserializes the value from JSON.
		 *
		 * @param element the key of the element to read
		 * @param json the JSON object
		 * @return the deserialized value
		 */
		public abstract T deserialize(String element, JsonObject json);

		/**
		 * Deserializes the value from a buffer.
		 *
		 * @param buf the buffer
		 * @return the deserialized value
		 */
		public abstract T deserialize(PacketByteBuf buf);

		/**
		 * Serializes the value to JSON, under the specified element key.
		 *
		 * @param value the value to serialize
		 * @param element the key to serialize it under
		 * @param json the JSON object
		 */
		public abstract void serialize(T value, String element, JsonObject json);

		/**
		 * Serializes the value to a buffer.
		 *
		 * @param value the value to serialize
		 * @param buf the buffer
		 */
		public abstract void serialize(T value, PacketByteBuf buf);

		@FunctionalInterface
		public interface RecipeFactory<T, R extends AbstractBrewingRecipe<T>> {
			R create(Identifier id, String group, T input, Ingredient ingredient, T output, int fuel, int brewTime);
		}
	}
}
