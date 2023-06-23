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

package org.quiltmc.qsl.block.extensions.api;

import java.util.function.Function;
import java.util.function.ToIntFunction;

import org.jetbrains.annotations.Contract;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.AbstractBlock.Settings;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.block.enums.NoteBlockInstrument;
import net.minecraft.block.piston.PistonBehavior;
import net.minecraft.entity.EntityType;
import net.minecraft.feature_flags.FeatureFlag;
import net.minecraft.feature_flags.FeatureFlagBitSet;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.block.extensions.mixin.AbstractBlockAccessor;
import org.quiltmc.qsl.block.extensions.mixin.AbstractBlockSettingsAccessor;

/**
 * An extended variant of the {@link AbstractBlock.Settings} class, which provides extra methods for customization.
 */
public class QuiltBlockSettings extends AbstractBlock.Settings {
	protected QuiltBlockSettings(AbstractBlock.Settings settings) {
		super();

		var otherAccessor = (AbstractBlockSettingsAccessor) settings;

		// region [VanillaCopy] AbstractBlock.Settings#copy(AbstractBlock.Settings)
		this.hardness(otherAccessor.getHardness());
		this.resistance(otherAccessor.getResistance());
		this.collidable(otherAccessor.getCollidable());
		this.ticksRandomly(otherAccessor.getRandomTicks());
		this.luminance(otherAccessor.getLuminance());
		this.mapColor(otherAccessor.getMapColorGetter());
		this.sounds(otherAccessor.getSoundGroup());
		this.slipperiness(otherAccessor.getSlipperiness());
		this.velocityMultiplier(otherAccessor.getVelocityMultiplier());
		this.dynamicBounds(otherAccessor.getDynamicBounds());
		this.opaque(otherAccessor.getOpaque());
		this.air(otherAccessor.getIsAir());
		this.lavaIgnitable(otherAccessor.getLavaIgnitable());
		this.liquid(otherAccessor.getLiquid());
		this.nonSolid(otherAccessor.getNonSolid());
		this.solid(otherAccessor.getSolid());
		this.pistonBehavior(otherAccessor.getPistonBehavior());
		this.requiresTool(otherAccessor.getToolRequired());
		((AbstractBlockSettingsAccessor) this).setOffsetFunction(otherAccessor.getOffsetFunction());
		this.spawnsParticlesOnBreak(otherAccessor.getSpawnsParticlesOnBreak());
		this.requiredFlags(otherAccessor.getRequiredFlags());
		this.emissiveLighting(otherAccessor.getEmissiveLightingPredicate());
		this.instrument(otherAccessor.getInstrument());
		this.replaceable(otherAccessor.getReplaceable());
		// endregion

		// also copy other stuff Vanilla doesn't bother with
		this.jumpVelocityMultiplier(otherAccessor.getJumpVelocityMultiplier());
		this.drops(otherAccessor.getLootTableId());
		this.allowsSpawning(otherAccessor.getAllowsSpawningPredicate());
		this.solidBlock(otherAccessor.getSolidBlockPredicate());
		this.suffocates(otherAccessor.getSuffocationPredicate());
		this.blockVision(otherAccessor.getBlockVisionPredicate());
		this.postProcess(otherAccessor.getPostProcessPredicate());
		this.emissiveLighting(otherAccessor.getEmissiveLightingPredicate());
	}

	public static QuiltBlockSettings copyOf(AbstractBlock block) {
		return new QuiltBlockSettings(((AbstractBlockAccessor) block).getSettings());
	}

	public static QuiltBlockSettings copyOf(AbstractBlock.Settings settings) {
		return new QuiltBlockSettings(settings);
	}

	@Override
	public QuiltBlockSettings noCollision() {
		super.noCollision();
		return this;
	}

	@Override
	public QuiltBlockSettings nonOpaque() {
		super.nonOpaque();
		return this;
	}

	@Override
	public QuiltBlockSettings slipperiness(float slipperiness) {
		super.slipperiness(slipperiness);
		return this;
	}

	@Override
	public QuiltBlockSettings velocityMultiplier(float velocityMultiplier) {
		super.velocityMultiplier(velocityMultiplier);
		return this;
	}

	@Override
	public QuiltBlockSettings jumpVelocityMultiplier(float jumpVelocityMultiplier) {
		super.jumpVelocityMultiplier(jumpVelocityMultiplier);
		return this;
	}

	@Override
	public QuiltBlockSettings sounds(BlockSoundGroup soundGroup) {
		super.sounds(soundGroup);
		return this;
	}

	@Override
	public QuiltBlockSettings luminance(ToIntFunction<BlockState> luminance) {
		super.luminance(luminance);
		return this;
	}

	@Override
	public QuiltBlockSettings strength(float hardness, float resistance) {
		super.strength(hardness, resistance);
		return this;
	}

	@Override
	public QuiltBlockSettings breakInstantly() {
		super.breakInstantly();
		return this;
	}

	@Override
	public QuiltBlockSettings strength(float strength) {
		super.strength(strength);
		return this;
	}

	@Override
	public QuiltBlockSettings ticksRandomly() {
		super.ticksRandomly();
		return this;
	}

	@Override
	public QuiltBlockSettings dynamicBounds() {
		super.dynamicBounds();
		return this;
	}

	@Override
	public QuiltBlockSettings dropsNothing() {
		super.dropsNothing();
		return this;
	}

	@Override
	public QuiltBlockSettings dropsLike(Block source) {
		super.dropsLike(source);
		return this;
	}

	@Override
	public QuiltBlockSettings lavaIgnitable() {
		super.lavaIgnitable();
		return this;
	}

	public QuiltBlockSettings liquid() {
		super.liquid();
		return this;
	}

	@Override
	@Contract("->this")
	public QuiltBlockSettings solid() {
		return this.solid(true);
	}

	@Override
	@Contract("->this")
	@Deprecated
	public QuiltBlockSettings nonSolid() {
		return this.nonSolid(true);
	}

	@Override
	public QuiltBlockSettings pistonBehavior(PistonBehavior pistonBehavior) {
		super.pistonBehavior(pistonBehavior);
		return this;
	}

	@Override
	public QuiltBlockSettings air() {
		super.air();
		return this;
	}

	@Override
	public QuiltBlockSettings allowsSpawning(AbstractBlock.TypedContextPredicate<EntityType<?>> predicate) {
		super.allowsSpawning(predicate);
		return this;
	}

	@Override
	public QuiltBlockSettings solidBlock(AbstractBlock.ContextPredicate predicate) {
		super.solidBlock(predicate);
		return this;
	}

	@Override
	public QuiltBlockSettings suffocates(AbstractBlock.ContextPredicate predicate) {
		super.suffocates(predicate);
		return this;
	}

	@Override
	public QuiltBlockSettings blockVision(AbstractBlock.ContextPredicate predicate) {
		super.blockVision(predicate);
		return this;
	}

	@Override
	public QuiltBlockSettings postProcess(AbstractBlock.ContextPredicate predicate) {
		super.postProcess(predicate);
		return this;
	}

	@Override
	public QuiltBlockSettings emissiveLighting(AbstractBlock.ContextPredicate predicate) {
		super.emissiveLighting(predicate);
		return this;
	}

	@Override
	public QuiltBlockSettings requiresTool() {
		super.requiresTool();
		return this;
	}

	@Override
	@Contract("_->this")
	public QuiltBlockSettings mapColor(DyeColor dyeColor) {
		super.mapColor(dyeColor);
		return this;
	}

	@Override
	public QuiltBlockSettings mapColor(MapColor color) {
		super.mapColor(color);
		return this;
	}

	@Override
	@Contract("_->this")
	public QuiltBlockSettings mapColor(Function<BlockState, MapColor> function) {
		super.mapColor(function);
		return this;
	}

	@Override
	public QuiltBlockSettings hardness(float hardness) {
		super.hardness(hardness);
		return this;
	}

	@Override
	public QuiltBlockSettings resistance(float resistance) {
		super.resistance(resistance);
		return this;
	}

	@Override
	public QuiltBlockSettings offsetType(AbstractBlock.OffsetType offsetType) {
		super.offsetType(offsetType);
		return this;
	}

	@Override
	public QuiltBlockSettings disableParticlesOnBreak() {
		super.disableParticlesOnBreak();
		return this;
	}

	@Override
	public QuiltBlockSettings requiredFlags(FeatureFlag... flags) {
		super.requiredFlags(flags);
		return this;
	}

	@Override
	@Contract("_->this")
	public QuiltBlockSettings instrument(NoteBlockInstrument instrument) {
		super.instrument(instrument);
		return this;
	}

	@Override
	@Contract("->this")
	public QuiltBlockSettings replaceable() {
		super.replaceable();
		return this;
	}

	// region Added by Quilt

	public QuiltBlockSettings collidable(boolean collidable) {
		((AbstractBlockSettingsAccessor) this).setCollidable(collidable);
		return this;
	}

	public QuiltBlockSettings opaque(boolean opaque) {
		((AbstractBlockSettingsAccessor) this).setOpaque(opaque);
		return this;
	}

	public QuiltBlockSettings ticksRandomly(boolean ticksRandomly) {
		((AbstractBlockSettingsAccessor) this).setRandomTicks(ticksRandomly);
		return this;
	}

	public QuiltBlockSettings dynamicBounds(boolean dynamicBounds) {
		((AbstractBlockSettingsAccessor) this).setDynamicBounds(dynamicBounds);
		return this;
	}

	public QuiltBlockSettings requiresTool(boolean requiresTool) {
		((AbstractBlockSettingsAccessor) this).setToolRequired(requiresTool);
		return this;
	}

	public QuiltBlockSettings air(boolean isAir) {
		((AbstractBlockSettingsAccessor) this).setIsAir(isAir);
		return this;
	}

	/**
	 * Sets whether this block can be set on fire by neighboring lava.
	 *
	 * @param ignitable {@code true} if this block can be set on fire by lava, or {@code false} otherwise
	 * @return {@code this} builder
	 * @see #lavaIgnitable()
	 */
	public QuiltBlockSettings lavaIgnitable(boolean ignitable) {
		((AbstractBlockSettingsAccessor) this).setLavaIgnitable(ignitable);
		return this;
	}

	public QuiltBlockSettings liquid(boolean liquid) {
		((AbstractBlockSettingsAccessor) this).setLiquid(liquid);
		return this;
	}

	public QuiltBlockSettings nonSolid(boolean nonSolid) {
		((AbstractBlockSettingsAccessor) this).setNonSolid(nonSolid);
		return this;
	}

	public QuiltBlockSettings solid(boolean solid) {
		((AbstractBlockSettingsAccessor) this).setSolid(solid);
		return this;
	}

	/**
	 * Sets the luminance of the block. The block will have this luminance regardless of its current state.
	 *
	 * @param luminance new luminance
	 * @return {@code this} builder
	 * @see #luminance(ToIntFunction)
	 */
	public QuiltBlockSettings luminance(int luminance) {
		return this.luminance(ignored -> luminance);
	}

	/**
	 * Sets the loot table identifier that this block will use when broken.
	 *
	 * @param dropTableId the new loot table identifier
	 * @return {@code this} builder
	 */
	public QuiltBlockSettings drops(Identifier dropTableId) {
		((AbstractBlockSettingsAccessor) this).setLootTableId(dropTableId);
		return this;
	}

	public QuiltBlockSettings spawnsParticlesOnBreak(boolean spawnsParticlesOnBreak) {
		((AbstractBlockSettingsAccessor) this).setSpawnsParticlesOnBreak(spawnsParticlesOnBreak);
		return this;
	}

	public Settings requiredFlags(FeatureFlagBitSet flags) {
		((AbstractBlockSettingsAccessor) this).setRequiredFlags(flags);
		return this;
	}

	public QuiltBlockSettings replaceable(boolean replaceable) {
		((AbstractBlockSettingsAccessor) this).setReplaceable(replaceable);
		return this;
	}

	// endregion
}
