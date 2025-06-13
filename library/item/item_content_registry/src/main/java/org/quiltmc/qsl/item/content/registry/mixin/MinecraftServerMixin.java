package org.quiltmc.qsl.item.content.registry.mixin;

import net.minecraft.server.MinecraftServer;
import org.quiltmc.qsl.item.content.registry.impl.ItemContentRegistriesInitializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftServer.class)
public class MinecraftServerMixin {
	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/FuelTimes;create(Lnet/minecraft/registry/HolderLookup$Provider;Lnet/minecraft/feature_flags/FeatureFlagBitSet;)Lnet/minecraft/block/entity/FuelTimes;"))
	private void startFuelTimeCollection(CallbackInfo ci) {
		ItemContentRegistriesInitializer.startInitialFuelCollection();
	}

	@Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/FuelTimes;create(Lnet/minecraft/registry/HolderLookup$Provider;Lnet/minecraft/feature_flags/FeatureFlagBitSet;)Lnet/minecraft/block/entity/FuelTimes;", shift = At.Shift.AFTER))
	private void endFuelTimeCollection(CallbackInfo ci) {
		ItemContentRegistriesInitializer.endInitialFuelCollection();
	}
}
