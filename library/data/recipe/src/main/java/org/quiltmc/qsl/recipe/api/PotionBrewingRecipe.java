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

package org.quiltmc.qsl.recipe.api;

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

import org.quiltmc.qsl.recipe.impl.RecipeImpl;

/**
 * A {@link Potion} brewing recipe.
 *
 * <p>
 *     The recipe has six arguments:
 * </p>
 * <ul>
 * 		<li>type: "quilt_recipe:potion_brewing"</li>
 * 		<li>group: A string representing the group of the recipe</li>
 * 		<li>ingredient: A valid ingredient json object.</li>
 * 		<li>input: A valid identifier for a {@link Potion potion}.</li>
 * 		<li>output: A valid identifier for a {@link Potion potion}.</li>
 * 		<li>fuel: An integer representing how much fuel this craft will take.
 * 			In vanilla, blaze powder supplies 20 fuel.</li>
 * 		<li>time: An integer representing how much time this craft will take, in ticks.
 * 			In vanilla, the default is 400 ticks.</li>
 * </ul>
 *
 * Here is an example recipe for a potion of luck that takes a water potion, a trapdoor of some kind, 5 fuel units, and 123 ticks.
 * <pre><code>
 * {
 *   "type": "quilt_recipe:potion_brewing",
 *   "ingredient": {
 *     "tag": "minecraft:trapdoors"
 *   },
 *   "input": "minecraft:water",
 *   "output": "minecraft:luck",
 *   "fuel": 5,
 *   "time": 123
 * }
 * </code></pre>
 */
public class PotionBrewingRecipe extends AbstractBrewingRecipe<Potion> {
	public PotionBrewingRecipe(Identifier id, String group, Potion input, Ingredient ingredient, Potion output, int fuel, int brewTime) {
		super(id, group, input, ingredient, output, fuel, brewTime);
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

	public static class Serializer<R extends PotionBrewingRecipe> extends AbstractBrewingSerializer<Potion, R> {
		public Serializer(RecipeFactory<Potion, R> recipeFactory) {
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
