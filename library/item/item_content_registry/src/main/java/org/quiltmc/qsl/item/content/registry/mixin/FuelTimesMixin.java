package org.quiltmc.qsl.item.content.registry.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMaps;
import net.minecraft.block.entity.FuelTimes;
import net.minecraft.item.Item;
import org.objectweb.asm.Opcodes;
import org.quiltmc.qsl.item.content.registry.impl.ItemContentRegistriesInitializer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(targets = {"net.minecraft.block.entity.FuelTimes"})
public abstract class FuelTimesMixin {
	@ModifyVariable(method = "<init>", at = @At(value = "FIELD", target = "net/minecraft/block/entity/FuelTimes.fuelTimes : Lit/unimi/dsi/fastutil/objects/Object2IntSortedMap;", opcode = Opcodes.PUTFIELD), argsOnly = true)
	private Object2IntSortedMap<Item> returnCachedMap(Object2IntSortedMap<Item> old) {
		if(!ItemContentRegistriesInitializer.FUEL_MAP.isEmpty()) {
			Object2IntSortedMap<Item> conversion = Object2IntSortedMaps.emptyMap();
			conversion.putAll(ItemContentRegistriesInitializer.FUEL_MAP);
			return conversion;
		}
		return old;
	}
}
