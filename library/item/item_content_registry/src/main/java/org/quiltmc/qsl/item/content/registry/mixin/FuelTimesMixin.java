package org.quiltmc.qsl.item.content.registry.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import net.minecraft.block.entity.FuelTimes;
import net.minecraft.item.Item;
import org.quiltmc.qsl.item.content.registry.impl.ItemContentRegistriesInitializer;
import org.spongepowered.asm.mixin.Debug;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(targets = {"net.minecraft.block.entity.FuelTimes"})
@Debug(export = true)
public abstract class FuelTimesMixin {
	@WrapOperation(method = "<init>", at = @At(value = "FIELD", target = "net/minecraft/block/entity/FuelTimes.fuelTimes : Lit/unimi/dsi/fastutil/objects/Object2IntSortedMap;"))
	private void returnCachedMap(FuelTimes instance, Object2IntSortedMap<Item> value, Operation<Void> original) {
		if(!ItemContentRegistriesInitializer.FUEL_MAP.isEmpty()) {
		 	// Is this actually right? I guess it was done this way in order for it to not have duplicate entries
			// But wouldn't the map sort it out for us?
			// I'm keeping this here to make it functionally equivalent with the old mixin.
			value.clear();
			value.putAll(ItemContentRegistriesInitializer.FUEL_MAP);
		}

		original.call(instance, value);
	}
}
