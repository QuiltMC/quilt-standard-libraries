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
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.recipe.impl.AbstractBrewingRecipe;

public class SwordImbuingTestRecipe extends AbstractBrewingRecipe<Item> {
	public SwordImbuingTestRecipe(Identifier id, Item input, Ingredient ingredient, Item output, int fuel) {
		super(id, input, ingredient, output, fuel);
	}

	@Override
	protected ItemStack craft(int slot, ItemStack input) {
		Multimap<EntityAttribute, EntityAttributeModifier> multimap = input.getAttributeModifiers(EquipmentSlot.MAINHAND);

		ItemStack output = new ItemStack(this.output);
		NbtCompound nbt = output.getOrCreateNbt();
		if (input.hasNbt()) {
			nbt.copyFrom(input.getNbt());
		}

		for (var entry : multimap.entries()) {
			EntityAttributeModifier modifier = entry.getValue();
			if (entry.getKey().equals(EntityAttributes.GENERIC_ATTACK_DAMAGE)) {
				modifier = new EntityAttributeModifier(
						modifier.getId(),
						modifier.getName(),
						modifier.getValue() + 5.0D,
						modifier.getOperation()
				);
			}
			output.addAttributeModifier(entry.getKey(), modifier, EquipmentSlot.MAINHAND);
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
