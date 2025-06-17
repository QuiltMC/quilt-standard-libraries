package org.quiltmc.qsl.item.content.registry.mixin;

import net.minecraft.block.entity.FuelTimes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.registry.tag.TagKey;
import org.quiltmc.qsl.item.content.registry.impl.ItemContentRegistriesInitializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(FuelTimes.Builder.class)
abstract class FuelTimesBuilderMixin {
	@Inject(
		method = "add(Lnet/minecraft/item/ItemConvertible;I)Lnet/minecraft/block/entity/FuelTimes$Builder;",
		at = @At("HEAD")
	)
	private void collectInitialItems(
		ItemConvertible item, int fuelTime, CallbackInfoReturnable<FuelTimes.Builder> cir
	) {
		if (ItemContentRegistriesInitializer.shouldCollectInitialFuels()) {
			ItemContentRegistriesInitializer.INITIAL_FUEL_ITEM_MAP.put(item.asItem(), fuelTime);
		}
	}

	@Inject(
		method = "add(Lnet/minecraft/registry/tag/TagKey;I)Lnet/minecraft/block/entity/FuelTimes$Builder;",
		at = @At("HEAD")
	)
	private void collectInitialTags(TagKey<Item> tag, int fuelTime, CallbackInfoReturnable<FuelTimes.Builder> cir) {
		if (ItemContentRegistriesInitializer.shouldCollectInitialFuels()) {
			ItemContentRegistriesInitializer.INITIAL_FUEL_TAG_MAP.put(tag, fuelTime);
		}
	}
}
