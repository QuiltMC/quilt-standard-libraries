package org.quiltmc.qsl.recipe.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.item.ItemStack;
import net.minecraft.tag.TagKey;

import org.quiltmc.qsl.recipe.impl.RecipeImpl;

@Mixin(targets = "net.minecraft.screen.BrewingStandScreenHandler$PotionSlot")
public class PotionSlotMixin {
	/**
	 * @author QuiltMC, Platymemo
	 * @reason Replaces the functionality with a {@link ItemStack#isIn(TagKey)} check for {@link RecipeImpl#POTIONS},
	 * and an {@link Overwrite} is more explicit than an {@link Inject} at {@code HEAD} and cancel.
	 */
	@Overwrite
	public static boolean matches(ItemStack stack) {
		return stack.isIn(RecipeImpl.POTIONS);
	}
}
