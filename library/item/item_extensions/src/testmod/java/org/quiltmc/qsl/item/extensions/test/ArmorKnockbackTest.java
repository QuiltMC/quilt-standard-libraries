package org.quiltmc.qsl.item.extension.test;

import static org.quiltmc.qsl.item.extension.test.BowsTest.MOD_ID;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;
import net.minecraft.world.World;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class ArmorKnockbackTest implements ModInitializer {
	private static final ArmorMaterial KNOCKBACK_RES_ARMOR = new ArmorMaterial() {
		@Override
		public int getDurability(EquipmentSlot slot) {
			return 1;
		}

		@Override
		public int getProtectionAmount(EquipmentSlot slot) {
			return 0;
		}

		@Override
		public int getEnchantability() {
			return 0;
		}

		@Override
		public SoundEvent getEquipSound() {
			return SoundEvents.BLOCK_AMETHYST_BLOCK_CHIME;
		}

		@Override
		public Ingredient getRepairIngredient() {
			return Ingredient.EMPTY;
		}

		@Override
		public String getName() {
			return "Knockback Res";
		}

		@Override
		public float getToughness() {
			return 0;
		}

		@Override
		public float getKnockbackResistance() {
			return 200;
		}
	};

	private static final ArmorItem KNOCKBACK_RES_CHESTPLATE = Registry.register(Registries.ITEM, new Identifier(MOD_ID, "knockback_res_chestplate"), new ArmorItem(KNOCKBACK_RES_ARMOR, EquipmentSlot.CHEST, new Item.Settings().maxCount(1).rarity(Rarity.RARE)) {
		@Override
		public void appendTooltip(ItemStack stack, @Nullable World world, List<Text> tooltip, TooltipContext context) {
			tooltip.add(Text.of("This tooltip should mention the knockback resistance."));
			super.appendTooltip(stack, world, tooltip, context);
		}
	});

	@Override
	public void onInitialize(ModContainer mod) { }
}
