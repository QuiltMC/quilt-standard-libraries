package org.quiltmc.qsl.recipe.impl;

import java.util.ArrayList;
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

public class CustomPotionBrewingRecipe extends PotionBrewingRecipe {
	private final List<StatusEffectInstance> statusEffects = new ArrayList<>();
	public CustomPotionBrewingRecipe(Identifier id, Potion input, Ingredient ingredient, Potion output, int fuel) {
		super(id, input, ingredient, output, fuel);
		PotionUtil.setCustomPotionEffects(this.ghostOutput, this.statusEffects);
	}

	@Override
	protected ItemStack craft(int slot, ItemStack input) {
		ItemStack output = super.craft(slot, input);

		var statusEffects = this.statusEffects;
		if (input.getOrCreateNbt().contains("CustomPotionEffects")) {
			statusEffects.addAll(PotionUtil.getCustomPotionEffects(input));
			PotionUtil.setCustomPotionEffects(output, statusEffects);
		}

		PotionUtil.setCustomPotionEffects(output, statusEffects);

		return output;
	}

	@Override
	public RecipeSerializer<CustomPotionBrewingRecipe> getSerializer() {
		return RecipeImpl.CUSTOM_POTION_SERIALIZER;
	}

	static class Serializer extends PotionBrewingRecipe.Serializer<CustomPotionBrewingRecipe> {
		Serializer(RecipeFactory<Potion, CustomPotionBrewingRecipe> recipeFactory) {
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
				boolean ambient = JsonHelper.getBoolean(jsonObject, "ambient", false);
				boolean showParticles = JsonHelper.getBoolean(jsonObject, "show_particles", true);
				boolean showIcon = JsonHelper.getBoolean(jsonObject, "show_icon", true);
				return new StatusEffectInstance(statusEffect, duration, amplifier, ambient, showParticles, showIcon);
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
