package org.quiltmc.qsl.recipe.impl;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BrewingStandBlockEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.PacketByteBuf;
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

public abstract class AbstractBrewingRecipe<T> implements Recipe<BrewingStandBlockEntity> {
	public static final List<Ingredient> VALID_INGREDIENTS = new ArrayList<>();
	protected final T input;
	protected final Ingredient ingredient;
	protected final T output;
	protected final int fuel;
	protected ItemStack ghostOutput;
	private final Identifier id;

	public AbstractBrewingRecipe(Identifier id, T input, Ingredient ingredient, T output, int fuel) {
		this.id = id;
		this.input = input;
		this.ingredient = ingredient;
		VALID_INGREDIENTS.add(ingredient);
		this.output = output;
		this.ghostOutput = new ItemStack(Items.POTION);
		this.fuel = fuel;
	}

	@Override
	public RecipeType<AbstractBrewingRecipe<?>> getType() {
		return Recipes.BREWING;
	}

	@Override
	public ItemStack craft(BrewingStandBlockEntity inventory) {
		for (int i = 0; i < 3; i++) {
			if (this.matches(i, inventory.getStack(i))) {
				inventory.setStack(i, craft(i, inventory.getStack(i)));
			}
		}

		return ItemStack.EMPTY;
	}

	/**
	 * Transforms the input {@link ItemStack} to the output {@link ItemStack}.
	 * <p>
	 * The input is guaranteed to match the required input, as defined by {@link #matches(int, ItemStack)}.</p>
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
				ItemStack stack = inventory.getStack(i);
				if (matches(i, stack)) {
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
	 * @return how much fuel this recipe takes to craft
	 */
	public int getFuelUse() {
		return fuel;
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

	protected static abstract class AbstractBrewingSerializer<T, R extends AbstractBrewingRecipe<T>> implements RecipeSerializer<R>, QuiltRecipeSerializer<R> {
		private final RecipeFactory<T, ? extends R> recipeFactory;

		protected AbstractBrewingSerializer(RecipeFactory<T, ? extends R> recipeFactory) {
			this.recipeFactory = recipeFactory;
		}

		@Override
		public R read(Identifier id, JsonObject json) {
			Ingredient ingredient = Ingredient.fromJson(JsonHelper.getObject(json, "ingredient"));
			T input = this.deserialize("input", json);
			T output = this.deserialize("output", json);
			int fuel = JsonHelper.getInt(json, "fuel", 1);
			return this.recipeFactory.create(id, input, ingredient, output, fuel);
		}

		@Override
		public R read(Identifier id, PacketByteBuf buf) {
			Ingredient ingredient = Ingredient.fromPacket(buf);
			T input = this.deserialize(buf);
			T output = this.deserialize(buf);
			int fuel = buf.readInt();
			return this.recipeFactory.create(id, input, ingredient, output, fuel);
		}

		@Override
		public void write(PacketByteBuf buf, R recipe) {
			recipe.ingredient.write(buf);
			this.serialize(recipe.input, buf);
			this.serialize(recipe.output, buf);
			buf.writeInt(recipe.fuel);
		}

		@Override
		public JsonObject toJson(R recipe) {
			JsonObject json = new JsonObject();
			json.add("ingredient", recipe.ingredient.toJson());
			json.addProperty("type", Registry.RECIPE_SERIALIZER.getId(recipe.getSerializer()).toString());
			this.serialize(recipe.input, "input", json);
			this.serialize(recipe.output, "output", json);
			json.addProperty("fuel", recipe.fuel);
			return json;
		}

		public abstract T deserialize(String element, JsonObject json);

		public abstract T deserialize(PacketByteBuf buf);

		public abstract void serialize(T value, String element, JsonObject json);

		public abstract void serialize(T value, PacketByteBuf buf);

		@FunctionalInterface
		public interface RecipeFactory<T, R extends AbstractBrewingRecipe<T>> {
			R create(Identifier id, T input, Ingredient ingredient, T output, int fuel);
		}
	}
}
