/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2021 The Quilt Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.block.extensions.mixin;

import java.util.function.Function;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.feature_flags.FeatureFlagBitSet;
import net.minecraft.loot.LootTable;
import net.minecraft.registry.RegistryKey;
import net.minecraft.sound.BlockSoundGroup;

@Mixin(AbstractBlock.Settings.class)
public interface AbstractBlockSettingsAccessor {
	// region Getters
	@Accessor
	float getHardness();

	@Accessor
	float getResistance();

	@Accessor
	boolean getCollidable();

	@Accessor
	boolean getRandomTicks();

	@Accessor
	ToIntFunction<BlockState> getLuminance();

	@Accessor
	Function<BlockState, MapColor> getMapColorGetter();

	@Accessor
	BlockSoundGroup getSoundGroup();

	@Accessor
	float getSlipperiness();

	@Accessor
	float getVelocityMultiplier();

	@Accessor
	float getJumpVelocityMultiplier();

	@Accessor
	boolean getDynamicBounds();

	@Accessor
	RegistryKey<LootTable> getLootTableId();

	@Accessor
	boolean getOpaque();

	@Accessor
	boolean getIsAir();

	@Accessor
	boolean getLavaIgnitable();

	@Accessor
	boolean getLiquid();

	@Accessor
	boolean getNonSolid();

	@Accessor
	boolean getSolid();

	@Accessor
	PistonBehavior getPistonBehavior();

	@Accessor
	boolean getToolRequired();

	@Accessor
	@Nullable
	AbstractBlock.OffsetFunction getOffsetFunction();

	@Accessor
	boolean getSpawnsDustParticles();

	@Accessor
	FeatureFlagBitSet getRequiredFlags();

	@Accessor
	AbstractBlock.TypedContextPredicate<EntityType<?>> getAllowsSpawningPredicate();

	@Accessor
	AbstractBlock.ContextPredicate getSolidBlockPredicate();

	@Accessor
	AbstractBlock.ContextPredicate getSuffocationPredicate();

	@Accessor
	AbstractBlock.ContextPredicate getBlockVisionPredicate();

	@Accessor
	AbstractBlock.ContextPredicate getPostProcessPredicate();

	@Accessor
	AbstractBlock.ContextPredicate getEmissiveLightingPredicate();

	@Accessor
	NoteBlockInstrument getInstrument();

	@Accessor
	boolean getReplaceable();
	// endregion

	// region Setters

	@Accessor
	void setCollidable(boolean collidable);

	@Accessor
	void setRandomTicks(boolean ticksRandomly);

	@Accessor
	void setLootTableId(RegistryKey<LootTable> lootTableId);

	@Accessor
	void setOpaque(boolean opaque);

	@Accessor
	void setIsAir(boolean isAir);

	@Accessor
	void setLavaIgnitable(boolean lavaIgnitable);

	@Accessor
	void setLiquid(boolean liquid);

	@Accessor
	void setNonSolid(boolean nonSolid);

	@Accessor
	void setSolid(boolean solid);

	@Accessor
	void setToolRequired(boolean toolRequired);

	@Accessor
	void setOffsetFunction(@Nullable AbstractBlock.OffsetFunction offsetFunction);

	@Accessor
	void setDynamicBounds(boolean dynamicBounds);

	@Accessor
	void setSpawnsDustParticles(boolean spawnsDustParticles);

	@Accessor
	void setRequiredFlags(FeatureFlagBitSet requiredFlags);

	@Accessor
	void setReplaceable(boolean replaceable);
	// endregion
}
