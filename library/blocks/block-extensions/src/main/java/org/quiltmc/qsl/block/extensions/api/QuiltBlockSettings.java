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

package org.quiltmc.qsl.block.extensions.api;

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;

import org.quiltmc.qsl.block.extensions.api.data.BlockDataKey;
import org.quiltmc.qsl.block.extensions.impl.BlockSettingsInternals;
import org.quiltmc.qsl.block.extensions.mixin.AbstractBlockAccessor;
import org.quiltmc.qsl.block.extensions.mixin.AbstractBlockSettingsAccessor;
import net.minecraft.block.*;
import net.minecraft.entity.EntityType;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.util.DyeColor;
import net.minecraft.util.Identifier;
import java.util.Map;
import java.util.function.Function;
import java.util.function.ToIntFunction;

/**
 * An extended variant of the {@link AbstractBlock.Settings} class, which provides extra methods for customization.
 */
public class QuiltBlockSettings extends AbstractBlock.Settings {
	protected QuiltBlockSettings(Material material, Function<BlockState, MapColor> mapColorProvider) {
		super(material, mapColorProvider);
	}

	protected QuiltBlockSettings(Material material, MapColor mapColor) {
		super(material, mapColor);
	}

	protected QuiltBlockSettings(AbstractBlock.Settings settings) {
		super(((AbstractBlockSettingsAccessor) settings).getMaterial(), ((AbstractBlockSettingsAccessor) settings).getMapColorProvider());

		var thisAccessor = (AbstractBlockSettingsAccessor) this;
		var otherAccessor = (AbstractBlockSettingsAccessor) settings;

		// region [VanillaCopy] AbstractBlock.Settings#copy(AbstractBlock.Settings)
		thisAccessor.setMaterial(otherAccessor.getMaterial());
		this.hardness(otherAccessor.getHardness());
		this.resistance(otherAccessor.getResistance());
		thisAccessor.setCollidable(otherAccessor.getCollidable());
		thisAccessor.setRandomTicks(otherAccessor.getRandomTicks());
		this.luminance(otherAccessor.getLuminance());
		thisAccessor.setMapColorProvider(otherAccessor.getMapColorProvider());
		this.sounds(otherAccessor.getSoundGroup());
		this.slipperiness(otherAccessor.getSlipperiness());
		this.velocityMultiplier(otherAccessor.getVelocityMultiplier());
		thisAccessor.setDynamicBounds(otherAccessor.getDynamicBounds());
		thisAccessor.setOpaque(otherAccessor.getOpaque());
		thisAccessor.setIsAir(otherAccessor.getIsAir());
		thisAccessor.setToolRequired(otherAccessor.isToolRequired());
		// endregion

		// also copy other stuff Vanilla doesn't bother with
		this.jumpVelocityMultiplier(otherAccessor.getJumpVelocityMultiplier());
		thisAccessor.setLootTableId(otherAccessor.getLootTableId());
		this.allowsSpawning(otherAccessor.getAllowsSpawningPredicate());
		this.solidBlock(otherAccessor.getSolidBlockPredicate());
		this.suffocates(otherAccessor.getSuffocationPredicate());
		this.blockVision(otherAccessor.getBlockVisionPredicate());
		this.postProcess(otherAccessor.getPostProcessPredicate());
		this.emissiveLighting(otherAccessor.getEmissiveLightingPredicate());

		// also copy extra data we added
		var thisInternals = (BlockSettingsInternals) this;
		var otherInternals = (BlockSettingsInternals) this;

		Map<BlockDataKey<?>, Object> settingsMap = otherInternals.qsl$getSettingsMap();
		if (settingsMap != null)
			thisInternals.qsl$setSettingsMap(new Reference2ObjectOpenHashMap<>(settingsMap));
	}

	public static QuiltBlockSettings of(Material material) {
		return of(material, material.getColor());
	}

	public static QuiltBlockSettings of(Material material, Function<BlockState, MapColor> mapColorProvider) {
		return new QuiltBlockSettings(material, mapColorProvider);
	}

	public static QuiltBlockSettings of(Material material, MapColor color) {
		return new QuiltBlockSettings(material, color);
	}

	public static QuiltBlockSettings of(Material material, DyeColor color) {
		return new QuiltBlockSettings(material, color.getMapColor());
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
	public QuiltBlockSettings mapColor(MapColor color) {
		super.mapColor(color);
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

	/* QUILT ADDITIONS */

	/**
	 * Sets the luminance of the block. The block will have this luminance regardless of its current state.
	 *
	 * @param luminance new luminance
	 * @return this builder
	 * @see #luminance(ToIntFunction)
	 */
	public QuiltBlockSettings luminance(int luminance) {
		return this.luminance(ignored -> luminance);
	}

	/**
	 * Sets the loot table ID that this block will use when broken.
	 *
	 * @param dropTableId new loot table ID
	 * @return this builder
	 */
	public QuiltBlockSettings drops(Identifier dropTableId) {
		((AbstractBlockSettingsAccessor) this).setLootTableId(dropTableId);
		return this;
	}

	/**
	 * Adds a key to value pair to the block's {@link org.quiltmc.qsl.block.extensions.api.data.ExtraBlockData ExtraBlockData} instance.
	 *
	 * @param key key
	 * @param value value
	 * @param <T> type of value
	 * @return this builder
	 */
	public <T> QuiltBlockSettings extraData(BlockDataKey<T> key, T value) {
		var internals = (BlockSettingsInternals) this;
		Map<BlockDataKey<?>, Object> map = internals.qsl$getSettingsMap();
		if (map == null)
			internals.qsl$setSettingsMap(map = new Reference2ObjectOpenHashMap<>());
		map.put(key, value);
		return this;
	}

	/* QUILT DELEGATE WRAPPERS */

	public QuiltBlockSettings mapColor(DyeColor color) {
		return this.mapColor(color.getMapColor());
	}
}
