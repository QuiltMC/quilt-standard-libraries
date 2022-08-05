package org.quiltmc.qsl.item.content.registry.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.item.content.registry.impl.ItemContentRegistriesInitializer;

@Mixin(targets = "net.minecraft.screen.BrewingStandScreenHandler$FuelSlot")
public class FuelSlotMixin {
	@Inject(method = "matches", at = @At("HEAD"), cancellable = true)
	private static void matchesBrewingFuelRegistry(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
		if (ItemContentRegistriesInitializer.BREWING_FUEL_MAP.containsKey(stack.getItem())) {
			cir.setReturnValue(true);
		}
	}
}
