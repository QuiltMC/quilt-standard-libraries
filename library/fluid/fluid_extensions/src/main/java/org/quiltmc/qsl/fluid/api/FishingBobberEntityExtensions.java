package org.quiltmc.qsl.fluid.api;

import net.minecraft.fluid.Fluid;
import net.minecraft.loot.LootTables;
import net.minecraft.tag.FluidTags;
import net.minecraft.tag.TagKey;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

public interface FishingBobberEntityExtensions {

	default TagKey<Fluid> canFishingbobberSwimOn() {return FluidTags.WATER;}

	default TagKey<Fluid> canFishingbobberCatchIn() {return FluidTags.WATER;}

	default Identifier fishingLootTable() {return LootTables.FISHING_GAMEPLAY;}
}
