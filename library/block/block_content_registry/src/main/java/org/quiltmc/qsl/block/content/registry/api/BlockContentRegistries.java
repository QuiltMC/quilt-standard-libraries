/*
 * Copyright 2022 QuiltMC
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


import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.event.GameEvent;

import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;

/**
 * Holds {@link RegistryEntryAttachment}s for different properties that blocks can hold.
 * <p>
 * Current properties:
 * <ul>
 *     <li>{@link #FLATTENABLE_BLOCKS}</li>
 *     <li>{@link #OXIDIZABLE_BLOCKS}</li>
 *     <li>{@link #WAXABLE_BLOCKS}</li>
 *     <li>{@link #STRIPPABLE_BLOCKS}</li>
 *     <li>{@link #FLAMMABLE_BLOCKS}</li>
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
	 * Values can be set via code and through a data-pack with the file {@code data/quilt/attachments/minecraft/block/flattenable_blocks.json}
	 */
	public static final RegistryEntryAttachment<Block, BlockState> FLATTENABLE_BLOCKS = RegistryEntryAttachment
			.builder(Registry.BLOCK,
					new Identifier(NAMESPACE, "flattenable_blocks"),
					BlockState.class,
					BlockState.CODEC)
			.build();

	/**
	 * A {@link RegistryEntryAttachment} for oxidizable blocks.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt/attachments/minecraft/block/oxidizable_blocks.json}
	 */
	public static final RegistryEntryAttachment<Block, ReversibleBlockEntry> OXIDIZABLE_BLOCKS = RegistryEntryAttachment
			.builder(Registry.BLOCK,
					new Identifier(NAMESPACE, "oxidizable_blocks"),
					ReversibleBlockEntry.class,
					ReversibleBlockEntry.CODEC)
			.build();

	/**
	 * A {@link RegistryEntryAttachment} for waxable blocks.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt/attachments/minecraft/block/waxable_blocks.json}
	 */
	public static final RegistryEntryAttachment<Block, ReversibleBlockEntry> WAXABLE_BLOCKS = RegistryEntryAttachment
			.builder(Registry.BLOCK,
					new Identifier(NAMESPACE, "waxable_blocks"),
					ReversibleBlockEntry.class,
					ReversibleBlockEntry.CODEC)
			.build();

	/**
	 * A {@link RegistryEntryAttachment} for strippable blocks.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt/attachments/minecraft/block/strippable_blocks.json}
	 */
	public static final RegistryEntryAttachment<Block, Block> STRIPPABLE_BLOCKS = RegistryEntryAttachment
			.builder(Registry.BLOCK,
					new Identifier(NAMESPACE, "strippable_blocks"),
					Block.class,
					Registry.BLOCK.getCodec().flatXmap(block -> {
						if (!block.getDefaultState().contains(Properties.AXIS)) {
							return DataResult.error("block does not contain AXIS property");
						}
						return DataResult.success(block);
					}, block -> {
						if (!block.getDefaultState().contains(Properties.AXIS)) {
							return DataResult.error("block does not contain AXIS property");
						}
						return DataResult.success(block);
					}))
			.build();

	/**
	 * A {@link RegistryEntryAttachment} for flammable blocks.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt/attachments/minecraft/block/flammable_blocks.json}
	 */
	public static final RegistryEntryAttachment<Block, FlammableBlockEntry> FLAMMABLE_BLOCKS = RegistryEntryAttachment
			.builder(Registry.BLOCK,
					new Identifier(NAMESPACE, "flammable_blocks"),
					FlammableBlockEntry.class,
					FlammableBlockEntry.CODEC)
			.build();
}

