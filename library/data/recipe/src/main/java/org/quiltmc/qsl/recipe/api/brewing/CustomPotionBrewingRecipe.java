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
import java.util.Collection;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
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
 * A {@link Potion} brewing recipe with extended functionality for custom potion effects.
 *
 * <p>
 * The recipe has seven arguments:
 * <ul>
 * 		<li>type: "quilt_recipe:custom_potion_brewing"</li>
 * 		<li>group: A string representing the group of the recipe</li>
 * 		<li>ingredient: A valid {@link net.minecraft.recipe.Ingredient ingredient} JSON object.</li>
 * 		<li>input: A valid {@link net.minecraft.util.Identifier identifier} for a {@link Potion potion}.</li>
 * 		<li>output: A valid {@link net.minecraft.util.Identifier identifier} for a {@link Potion potion}.</li>
 * 		<li>fuel: An integer representing how much fuel this craft will take.
 * 			In vanilla, blaze powder supplies {@code 20} fuel.</li>
 * 		<li>time: An integer representing how much time this craft will take, in ticks.
 * 			In vanilla, the default is {@code 400} ticks.</li>
 * 		<li>effects: An array holding all of the custom effects to add to the potion.
 * 			Each entry is either a valid {@link net.minecraft.util.Identifier identifier} for a {@link StatusEffect status effect} or a JSON object of the form:</li>
 * 		<ul>
 * 		    <li>type: A valid {@link net.minecraft.util.Identifier identifier} for a {@link StatusEffect status effect}.</li>
 * 		    <li>duration: An integer representing how long this effect lasts, in ticks.</li>
 * 		    <li>amplifier: An integer representing what level this effect is. Note that this is zero-based.</li>
 * 		    <li>particles: {@code true} if the effect should produce particles, or {@code false}.</li>
 * 		    <li>icon: {@code true} if the effect should show an icon in the HUD, or {@code false}.</li>
 * 		</ul>
 * </ul>
 *
 * Here is an example recipe for a mundane potion that takes a water potion, a block of dirt, {@code 20} fuel units, and {@code 200} ticks.
 * It additionally provides jump boost for a second, and immense strength for half a second.
 * <pre><code>
 * {
 *   "type": "quilt_recipe:custom_potion_brewing",
 *   "ingredient": {
 *     "item": "minecraft:dirt"
 *   },
 *   "input": "minecraft:water",
 *   "output": "minecraft:mundane",
 *   "fuel": 20,
 *   "time": 200,
 *   "effects": [
 *     "minecraft:jump_boost",
 *     {
 *       "type": "minecraft:strength",
 *       "amplifier": 123,
 *       "duration": 10
 *     }
 *   ]
 * }
 * </code></pre>
 * @see PotionUtil#getCustomPotionEffects(ItemStack)
 */
public class CustomPotionBrewingRecipe extends PotionBrewingRecipe {
	private final List<StatusEffectInstance> statusEffects = new ArrayList<>();
	public CustomPotionBrewingRecipe(Identifier id, String group, Potion input, Ingredient ingredient, Potion output, int fuel, int brewTime) {
		super(id, group, input, ingredient, output, fuel, brewTime);
		PotionUtil.setCustomPotionEffects(this.ghostOutput, this.statusEffects);
	}

	public CustomPotionBrewingRecipe(Identifier id, String group, Potion input, Ingredient ingredient, Potion output, int fuel, int brewTime, Collection<StatusEffectInstance> effects) {
		this(id, group, input, ingredient, output, fuel, brewTime);
		this.statusEffects.addAll(effects);
		PotionUtil.setCustomPotionEffects(this.ghostOutput, this.statusEffects);
	}

	@Override
	protected ItemStack craft(int slot, ItemStack input) {
		ItemStack output = super.craft(slot, input);

		var statusEffects = this.statusEffects;
		if (input.getOrCreateNbt().contains("CustomPotionEffects")) {
			statusEffects = new ArrayList<>(this.statusEffects);
			statusEffects.addAll(PotionUtil.getCustomPotionEffects(input));
		}

		PotionUtil.setCustomPotionEffects(output, statusEffects);

		return output;
	}

	@Override
	public RecipeSerializer<CustomPotionBrewingRecipe> getSerializer() {
		return RecipeImpl.CUSTOM_POTION_SERIALIZER;
	}

	public static class Serializer extends PotionBrewingRecipe.Serializer<CustomPotionBrewingRecipe> {
		public Serializer(RecipeFactory<Potion, CustomPotionBrewingRecipe> recipeFactory) {
			super(recipeFactory);
		}

		@Override
		public CustomPotionBrewingRecipe read(Identifier id, JsonObject json) {
			CustomPotionBrewingRecipe recipe = super.read(id, json);

			if (json.has("effects")) {
				recipe.statusEffects.clear();
				for(JsonElement effectJson : JsonHelper.getArray(json, "effects")) {
					StatusEffectInstance instance;
					instance = readStatusEffectInstance(effectJson);
					recipe.statusEffects.add(instance);
				}
			}

			return recipe;
		}

		private StatusEffectInstance readStatusEffectInstance(JsonElement json) {
			if (json instanceof JsonObject jsonObject) {
				String string = JsonHelper.getString(jsonObject, "type");
				StatusEffect statusEffect = Registry.STATUS_EFFECT
						.getOrEmpty(new Identifier(string))
						.orElseThrow(() -> new JsonSyntaxException("Unknown status effect '" + string + "'"));
				int duration = JsonHelper.getInt(jsonObject, "duration", 20);
				int amplifier = JsonHelper.getInt(jsonObject, "amplifier", 0);
				boolean showParticles = JsonHelper.getBoolean(jsonObject, "particles", true);
				boolean showIcon = JsonHelper.getBoolean(jsonObject, "icon", true);
				return new StatusEffectInstance(statusEffect, duration, amplifier, false, showParticles, showIcon);
			} else { // default to a basic 20 tick 0 amplifier status effect
				String string = json.getAsString();
				StatusEffect statusEffect = Registry.STATUS_EFFECT
						.getOrEmpty(new Identifier(string))
						.orElseThrow(() -> new JsonSyntaxException("Unknown status effect '" + string + "'"));
				return new StatusEffectInstance(statusEffect, 20);
			}
		}

		@Override
		public void write(PacketByteBuf buf, CustomPotionBrewingRecipe recipe) {
			super.write(buf, recipe);
			NbtCompound nbt = new NbtCompound();
			NbtList statusEffectsNbt = new NbtList();
			recipe.statusEffects.forEach(effect -> {
				NbtCompound effectNbt = new NbtCompound();
				effect.writeNbt(effectNbt);
				statusEffectsNbt.add(effectNbt);
			});
			nbt.put("CustomPotionEffects", statusEffectsNbt);
			buf.writeNbt(nbt);
		}

		@Override
		public CustomPotionBrewingRecipe read(Identifier id, PacketByteBuf buf) {
			CustomPotionBrewingRecipe recipe = super.read(id, buf);
			recipe.statusEffects.clear();
			PotionUtil.getCustomPotionEffects(buf.readNbt(), recipe.statusEffects);
			return recipe;
		}
	}


}
