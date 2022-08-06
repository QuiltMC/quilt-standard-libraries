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

import com.google.gson.JsonObject;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.PotionItem;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.recipe.impl.RecipeImpl;

/**
 * A {@link PotionItem} brewing recipe.
 *
 * <p>
 *     The recipe has six arguments:
 * </p>
 * <ul>
 * 		<li>type: "quilt_recipe:potion_item_brewing"</li>
 * 		<li>group: A string representing the group of the recipe</li>
 * 		<li>ingredient: A valid ingredient json object.</li>
 * 		<li>input: A valid identifier for an {@link Item item}.</li>
 * 		<li>output: A valid identifier for an {@link Item item}.</li>
 * 		<li>fuel: An integer representing how much fuel this craft will take.
 * 			In vanilla, blaze powder supplies 20 fuel.</li>
 * 		<li>time: An integer representing how much time this craft will take, in ticks.
 * 			In vanilla, the default is 400 ticks.</li>
 * </ul>
 *
 * Here is an example recipe for transforming a regular potion into a lingering potion using a log, 20 fuel units, and 100 ticks.
 * <pre><code>
 * {
 *   "type": "quilt_recipe:potion_item_brewing",
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
