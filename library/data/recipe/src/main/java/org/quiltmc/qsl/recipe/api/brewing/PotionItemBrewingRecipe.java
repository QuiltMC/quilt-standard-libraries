/*
 * Copyright 2023 QuiltMC
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

import com.google.gson.JsonObject;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import org.quiltmc.qsl.recipe.impl.RecipeImpl;

/**
 * A {@link PotionItem} brewing recipe.
 *
 * <p>
 * The recipe has six arguments:
 * <ul>
 * 		<li>type: "quilt:potion_item_brewing"</li>
 * 		<li>group: A string representing the group of the recipe</li>
 * 		<li>ingredient: A valid {@link net.minecraft.recipe.Ingredient ingredient} JSON object.</li>
 * 		<li>input: A valid {@link net.minecraft.util.Identifier identifier} for an {@link Item item}.</li>
 * 		<li>output: A valid {@link net.minecraft.util.Identifier identifier} for an {@link Item item}.</li>
 * 		<li>fuel: An integer representing how much fuel this craft will take.
 * 			In vanilla, blaze powder supplies {@code 20} fuel.</li>
 * 		<li>time: An integer representing how much time this craft will take, in ticks.
 * 			In vanilla, the default is {@code 400} ticks.</li>
 * </ul>
 * Here is an example recipe for transforming a regular potion into a lingering potion using a log, {@code 20} fuel units, and {@code 100} ticks.
 * <pre><code>
 * {
 *   "type": "quilt:potion_item_brewing",
 *   "ingredient": {
 *     "tag": "minecraft:logs"
 *   },
 *   "input": "minecraft:potion",
 *   "output": "minecraft:lingering_potion",
 *   "fuel": 20,
 *   "time": 100
 * }
 * </code></pre>
 */
public class PotionItemBrewingRecipe extends AbstractBrewingRecipe<Item> {
	public PotionItemBrewingRecipe(Identifier id, String group, Item input, Ingredient ingredient, Item output, int fuel, int brewTime) {
		super(id, group, input, ingredient, output, fuel, brewTime);
		this.result = new ItemStack(this.output);
	}

	@Override
	protected ItemStack craft(int slot, ItemStack input) {
		var output = new ItemStack(this.output);
		PotionUtil.setPotion(output, PotionUtil.getPotion(input));

		if (input.getOrCreateNbt().contains("CustomPotionEffects")) {
			PotionUtil.setCustomPotionEffects(output, PotionUtil.getCustomPotionEffects(input));
		}

		return output;
	}

	@Override
	public boolean matches(int slot, ItemStack input) {
		return input.isOf(this.input);
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeImpl.POTION_ITEM_SERIALIZER;
	}

	public static class Serializer extends AbstractBrewingSerializer<Item, PotionItemBrewingRecipe> {
		public Serializer(RecipeFactory<Item, PotionItemBrewingRecipe> recipeFactory) {
			super(recipeFactory);
		}

		@Override
		public Item deserialize(String element, JsonObject json) {
			return JsonHelper.getItem(json, element);
		}

		@Override
		public Item deserialize(PacketByteBuf buf) {
			return buf.readFromIterable(Registries.ITEM);
		}

		@Override
		public void serialize(Item item, String element, JsonObject json) {
			json.addProperty(element, Registries.ITEM.getId(item).toString());
		}

		@Override
		public void serialize(Item item, PacketByteBuf buf) {
			buf.writeFromIterable(Registries.ITEM, item);
		}
	}
}
