/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.block.content.registry.api;

import com.mojang.serialization.DataResult;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.block.content.registry.api.enchanting.EnchantingBooster;
import org.quiltmc.qsl.block.content.registry.api.enchanting.EnchantingBoosters;
import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;

/**
 * Holds {@link RegistryEntryAttachment}s for different properties that blocks can hold.
 * <p>
 * Current properties:
 * <ul>
 *     <li>{@link #FLATTENABLE}</li>
 *     <li>{@link #OXIDIZABLE}</li>
 *     <li>{@link #WAXABLE}</li>
 *     <li>{@link #STRIPPABLE}</li>
 *     <li>{@link #FLAMMABLE}</li>
 * 	   <li>{@link #ENCHANTING_BOOSTERS}</li>
 * </ul>
 */
public class BlockContentRegistries {
	/**
	 * The namespace for the content registries.
	 */
	public static final String NAMESPACE = "quilt";

	/**
	 * A {@link RegistryEntryAttachment} for when blocks are right clicked by a shovel.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt/attachments/minecraft/block/flattenable.json}
	 */
	public static final RegistryEntryAttachment<Block, BlockState> FLATTENABLE = RegistryEntryAttachment
			.builder(Registries.BLOCK,
					new Identifier(NAMESPACE, "flattenable"),
					BlockState.class,
					BlockState.CODEC)
			.build();

	/**
	 * A {@link RegistryEntryAttachment} for oxidizable blocks.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt/attachments/minecraft/block/oxidizable.json}
	 */
	public static final RegistryEntryAttachment<Block, ReversibleBlockEntry> OXIDIZABLE = RegistryEntryAttachment
			.builder(Registries.BLOCK,
					new Identifier(NAMESPACE, "oxidizable"),
					ReversibleBlockEntry.class,
					ReversibleBlockEntry.CODEC)
			.build();

	/**
	 * A {@link RegistryEntryAttachment} for waxable blocks.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt/attachments/minecraft/block/waxable.json}
	 */
	public static final RegistryEntryAttachment<Block, ReversibleBlockEntry> WAXABLE = RegistryEntryAttachment
			.builder(Registries.BLOCK,
					new Identifier(NAMESPACE, "waxable"),
					ReversibleBlockEntry.class,
					ReversibleBlockEntry.CODEC)
			.build();

	/**
	 * A {@link RegistryEntryAttachment} for strippable blocks.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt/attachments/minecraft/block/strippable.json}
	 */
	public static final RegistryEntryAttachment<Block, Block> STRIPPABLE = RegistryEntryAttachment
			.builder(Registries.BLOCK,
					new Identifier(NAMESPACE, "strippable"),
					Block.class,
					Registries.BLOCK.getCodec().flatXmap(block -> {
						if (!block.getDefaultState().contains(Properties.AXIS)) {
							return DataResult.error(() -> "block does not contain AXIS property");
						}

						return DataResult.success(block);
					}, block -> {
						if (!block.getDefaultState().contains(Properties.AXIS)) {
							return DataResult.error(() -> "block does not contain AXIS property");
						}

						return DataResult.success(block);
					}))
			.build();

	/**
	 * A {@link RegistryEntryAttachment} for flammable blocks.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt/attachments/minecraft/block/flammable.json}
	 */
	public static final RegistryEntryAttachment<Block, FlammableBlockEntry> FLAMMABLE = RegistryEntryAttachment
			.builder(Registries.BLOCK,
					new Identifier(NAMESPACE, "flammable"),
					FlammableBlockEntry.class,
					FlammableBlockEntry.CODEC)
			.build();

	/**
	 * A {@link RegistryEntryAttachment} for enchanting boosters in bookshelf equivalents.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt/attachments/minecraft/block/enchanting_boosters.json}
	 */
	public static final RegistryEntryAttachment<Block, EnchantingBooster> ENCHANTING_BOOSTERS = RegistryEntryAttachment
			.builder(Registries.BLOCK,
					new Identifier(NAMESPACE, "enchanting_boosters"),
					EnchantingBooster.class,
					EnchantingBoosters.CODEC)
			.build();
}

