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

package org.quiltmc.qsl.recipe.test;

import com.google.common.collect.Multimap;
import com.google.gson.JsonObject;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RecipeSerializer;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import org.quiltmc.qsl.recipe.api.brewing.AbstractBrewingRecipe;

public class SwordImbuingTestRecipe extends AbstractBrewingRecipe<Item> {
	public SwordImbuingTestRecipe(Identifier id, String group, Item input, Ingredient ingredient, Item output, int fuel, int brewTime) {
		super(id, group, input, ingredient, output, fuel, brewTime);
	}

	@Override
	protected ItemStack craft(int slot, ItemStack input) {
		Multimap<EntityAttribute, EntityAttributeModifier> multimap = input.getAttributeModifiers(EquipmentSlot.MAINHAND);

		ItemStack output = new ItemStack(this.output);
		NbtCompound nbt = output.getOrCreateNbt();
		if (input.hasNbt()) {
			nbt.copyFrom(input.getNbt());
			nbt.remove("AttributeModifiers");
		}

		for (var entry : multimap.entries()) {
			EntityAttribute attribute = entry.getKey();
			EntityAttributeModifier modifier = entry.getValue();
			if (attribute.equals(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
				// Add 3 to the attack damage
				modifier = new EntityAttributeModifier(
						modifier.getId(),
						modifier.getName(),
						modifier.getValue() + 3.0D,
						modifier.getOperation()
				);
			}

			output.addAttributeModifier(attribute, modifier, EquipmentSlot.MAINHAND);
		}

		return output;
	}

	@Override
	public boolean matches(int slot, ItemStack input) {
		return input.getItem() instanceof SwordItem;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return RecipeTestMod.TEST_SERIALIZER;
	}

	static class SwordImbuingTestSerializer extends AbstractBrewingRecipe.AbstractBrewingSerializer<Item, SwordImbuingTestRecipe> {
		protected SwordImbuingTestSerializer(AbstractBrewingSerializer.RecipeFactory<Item, SwordImbuingTestRecipe> recipeFactory) {
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
