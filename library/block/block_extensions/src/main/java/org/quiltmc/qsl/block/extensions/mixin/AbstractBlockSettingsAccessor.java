/*
 * Copyright 2021 QuiltMC
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

import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.function.Function;
import java.util.function.ToIntFunction;

@Mixin(AbstractBlock.Settings.class)
public interface AbstractBlockSettingsAccessor {
	// region Getters
	@Accessor("material")
	Material qsl$getMaterial();

	@Accessor("hardness")
	float qsl$getHardness();

	@Accessor("resistance")
	float qsl$getResistance();

	@Accessor("collidable")
	boolean qsl$getCollidable();

	@Accessor("randomTicks")
	boolean qsl$getRandomTicks();

	@Accessor("luminance")
	ToIntFunction<BlockState> qsl$getLuminance();

	@Accessor("mapColorProvider")
	Function<BlockState, MapColor> qsl$getMapColorProvider();

	@Accessor("soundGroup")
	BlockSoundGroup qsl$getSoundGroup();

	@Accessor("slipperiness")
	float qsl$getSlipperiness();

	@Accessor("velocityMultiplier")
	float qsl$getVelocityMultiplier();

	@Accessor("jumpVelocityMultiplier")
	float qsl$getJumpVelocityMultiplier();

	@Accessor("lootTableId")
	Identifier qsl$getLootTableId();

	@Accessor("opaque")
	boolean qsl$getOpaque();

	@Accessor("isAir")
	boolean qsl$getIsAir();

	@Accessor("toolRequired")
	boolean qsl$isToolRequired();

	@Accessor("allowsSpawningPredicate")
	AbstractBlock.TypedContextPredicate<EntityType<?>> qsl$getAllowsSpawningPredicate();

	@Accessor("solidBlockPredicate")
	AbstractBlock.ContextPredicate qsl$getSolidBlockPredicate();

	@Accessor("suffocationPredicate")
	AbstractBlock.ContextPredicate qsl$getSuffocationPredicate();

	@Accessor("blockVisionPredicate")
	AbstractBlock.ContextPredicate qsl$getBlockVisionPredicate();

	@Accessor("postProcessPredicate")
	AbstractBlock.ContextPredicate qsl$getPostProcessPredicate();

	@Accessor("emissiveLightingPredicate")
	AbstractBlock.ContextPredicate qsl$getEmissiveLightingPredicate();

	@Accessor("dynamicBounds")
	boolean qsl$getDynamicBounds();
	// endregion

	// region Setters
	@Accessor("material")
	void qsl$setMaterial(Material material);

	@Accessor("collidable")
	void qsl$setCollidable(boolean collidable);

	@Accessor("randomTicks")
	void qsl$setRandomTicks(boolean ticksRandomly);

	@Accessor("mapColorProvider")
	void qsl$setMapColorProvider(Function<BlockState, MapColor> mapColorProvider);

	@Accessor("lootTableId")
	void qsl$setLootTableId(Identifier lootTableId);

	@Accessor("opaque")
	void qsl$setOpaque(boolean opaque);

	@Accessor("isAir")
	void qsl$setIsAir(boolean isAir);

	@Accessor("toolRequired")
	void qsl$setToolRequired(boolean toolRequired);

	@Accessor("dynamicBounds")
	void qsl$setDynamicBounds(boolean dynamicBounds);
	// endregion
}
