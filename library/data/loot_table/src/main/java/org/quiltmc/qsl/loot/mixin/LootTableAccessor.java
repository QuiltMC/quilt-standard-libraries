package org.quiltmc.qsl.loot.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.loot.LootPool;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.function.LootFunction;

@Mixin(LootTable.class)
public interface LootTableAccessor {
	@Accessor
	LootPool[] getPools();

	@Accessor
	LootFunction[] getFunctions();
}
