package org.quiltmc.qsl.enchantment.mixin;

import net.minecraft.util.collection.Weight;
import net.minecraft.util.collection.Weighted;
import org.quiltmc.qsl.enchantment.mixinterface.MutableWeight;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Weighted.Absent.class)
public class WeightedAbsentMixin implements MutableWeight {
	@Shadow
	@Final
	@Mutable
	private Weight weight;

	public void setWeight(Weight weight) {
		this.weight = weight;
	}
}
