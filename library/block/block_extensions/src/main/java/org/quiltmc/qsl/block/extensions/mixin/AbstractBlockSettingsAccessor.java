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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.Material;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;

@Mixin(AbstractBlock.Settings.class)
public interface AbstractBlockSettingsAccessor {
	// region Getters
	@Accessor
	Material getMaterial();

	@Accessor
	float getHardness();

	@Accessor
	float getResistance();

	@Accessor
	boolean getCollidable();

	@Accessor
	boolean getRandomTicks();

	@Accessor("luminance")
	ToIntFunction<BlockState> getLuminance();

	@Accessor
	Function<BlockState, MapColor> getMapColorProvider();

	@Accessor
	BlockSoundGroup getSoundGroup();

	@Accessor
	float getSlipperiness();

	@Accessor
	float getVelocityMultiplier();

	@Accessor
	float getJumpVelocityMultiplier();

	@Accessor
	Identifier getLootTableId();

	@Accessor
	boolean getOpaque();

	@Accessor
	boolean getIsAir();

	@Accessor
	boolean isToolRequired();

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
	boolean getDynamicBounds();
	// endregion

	// region Setters
	@Accessor
	void setMaterial(Material material);

	@Accessor
	void setCollidable(boolean collidable);

	@Accessor
	void setRandomTicks(boolean ticksRandomly);

	@Accessor
	void setMapColorProvider(Function<BlockState, MapColor> mapColorProvider);

	@Accessor
	void setLootTableId(Identifier lootTableId);

	@Accessor
	void setOpaque(boolean opaque);

	@Accessor
	void setIsAir(boolean isAir);

	@Accessor
	void setToolRequired(boolean toolRequired);

	@Accessor
	void setDynamicBounds(boolean dynamicBounds);
	// endregion
}
