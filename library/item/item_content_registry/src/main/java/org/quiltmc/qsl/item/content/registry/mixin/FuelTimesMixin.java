package org.quiltmc.qsl.item.content.registry.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import net.minecraft.block.entity.FuelTimes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.quiltmc.qsl.item.content.registry.api.ItemContentRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.SequencedSet;

@Mixin(FuelTimes.class)
public class FuelTimesMixin {
	// Mixins are redirects here because we don't want to silently error if another mod is incompatible here

	@Redirect(method = "isFuel", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2IntSortedMap;containsKey(Ljava/lang/Object;)Z", remap = false))
	private boolean isFuelWithREA(Object2IntSortedMap<Item> instance, Object o, @Local(argsOnly = true) ItemStack stack) {
		return ItemContentRegistries.FUEL_TIMES.get(stack.getItem()).isPresent();
	}

	@Redirect(method = "validItems", at = @At(value = "INVOKE", target = "Ljava/util/Collections;unmodifiableSequencedSet(Ljava/util/SequencedSet;)Ljava/util/SequencedSet;", remap = false))
	private SequencedSet<Item> validFuelsWithREA(SequencedSet<? extends Item> instance) {
		SequencedSet<Item> items = new LinkedHashSet<>();
		ItemContentRegistries.FUEL_TIMES.forEach((entry) -> items.add(entry.entry()));

		return Collections.unmodifiableSequencedSet(items);
	}

	@Redirect(method = "getFuelTime", at = @At(value = "INVOKE", target = "Lit/unimi/dsi/fastutil/objects/Object2IntSortedMap;getInt(Ljava/lang/Object;)I", remap = false))
	public int getFuelTimeWithREA(Object2IntSortedMap<Item> instance, Object o, @Local(argsOnly = true) ItemStack stack) {
		return ItemContentRegistries.FUEL_TIMES.get(stack.getItem()).orElse(0);
	}
}

