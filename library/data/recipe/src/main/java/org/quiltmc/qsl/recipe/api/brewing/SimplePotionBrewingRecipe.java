/*
 * Copyright 2022-2023 QuiltMC
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

import java.util.HashSet;
import java.util.Set;

import com.google.gson.JsonObject;

import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtil;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import org.quiltmc.qsl.recipe.impl.RecipeImpl;

/**
 * A {@link Potion} brewing recipe.
 *
 * <p>
 * The recipe has six arguments:
 * <ul>
 * 		<li>type: "quilt:potion_brewing"</li>
 * 		<li>group: A string representing the group of the recipe</li>
 * 		<li>ingredient: A valid {@link net.minecraft.recipe.Ingredient ingredient} JSON object.</li>
 * 		<li>input: A valid {@link net.minecraft.util.Identifier identifier} for a {@link Potion potion}.</li>
 * 		<li>output: A valid {@link net.minecraft.util.Identifier identifier} for a {@link Potion potion}.</li>
 * 		<li>fuel: An integer representing how much fuel this craft will take.
 * 			In vanilla, blaze powder supplies {@code 20} fuel.</li>
 * 		<li>time: An integer representing how much time this craft will take, in ticks.
 * 			In vanilla, the default is {@code 400} ticks.</li>
 * </ul>
 * Here is an example recipe for a potion of luck that takes a water potion, a trapdoor of some kind, {@code 5} fuel units, and {@code 123} ticks.
 * <pre><code>
 * {
 *   "type": "quilt:potion_brewing",
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
public class SimplePotionBrewingRecipe extends AbstractBrewingRecipe<Potion> {
	public static final Set<Potion> BREWABLE_POTIONS = new HashSet<>();

	public SimplePotionBrewingRecipe(Identifier id, String group, Potion input, Ingredient ingredient, Potion output, int fuel, int brewTime) {
		super(id, group, input, ingredient, output, fuel, brewTime);
		BREWABLE_POTIONS.add(output);
		PotionUtil.setPotion(this.result, this.output);
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
	public RecipeSerializer<? extends SimplePotionBrewingRecipe> getSerializer() {
		return RecipeImpl.POTION_SERIALIZER;
	}

	public static class Serializer<R extends SimplePotionBrewingRecipe> extends AbstractBrewingSerializer<Potion, R> {
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
			json.addProperty(element, Registries.POTION.getId(value).toString());
		}

		@Override
		public void serialize(Potion value, PacketByteBuf buf) {
			buf.writeString(Registries.POTION.getId(value).toString());
		}
	}
}
