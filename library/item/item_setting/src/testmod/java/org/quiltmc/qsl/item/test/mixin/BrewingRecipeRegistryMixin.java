package org.quiltmc.qsl.item.test.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.item.Item;
import net.minecraft.potion.Potion;
import net.minecraft.potion.Potions;
import net.minecraft.recipe.BrewingRecipeRegistry;

import org.quiltmc.qsl.item.test.QuiltItemSettingsTests;

@Mixin(BrewingRecipeRegistry.class)
public abstract class BrewingRecipeRegistryMixin {
	@Shadow
	private static void registerPotionRecipe(Potion input, Item item, Potion output) {
		throw new IllegalStateException("Mixin injection failed.");
	}

	@Inject(method = "registerDefaults", at = @At("TAIL"))
	private static void registerRemainder(CallbackInfo ci) {
		registerPotionRecipe(Potions.WATER, QuiltItemSettingsTests.POTION_INGREDIENT_REMAINDER, Potions.FIRE_RESISTANCE);
	}
}
