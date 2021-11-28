package org.quiltmc.qsl.loot.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.loot.LootPool;
import net.minecraft.loot.condition.LootCondition;
import net.minecraft.loot.entry.LootPoolEntry;
import net.minecraft.loot.function.LootFunction;
import net.minecraft.loot.provider.number.LootNumberProvider;

@Mixin(LootPool.class)
public interface LootPoolAccessor {
	@Accessor
	LootPoolEntry[] getEntries();

	@Accessor
	LootCondition[] getConditions();

	@Accessor
	LootFunction[] getFunctions();

	@Accessor
	LootNumberProvider getRolls();

	@Accessor
	LootNumberProvider getBonusRolls();
}
