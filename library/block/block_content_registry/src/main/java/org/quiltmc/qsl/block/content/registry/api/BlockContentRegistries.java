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
 *     <li>{@link #FLATTENABLE_BLOCK}</li>
 *     <li>{@link #OXIDIZABLE_BLOCK}</li>
 *     <li>{@link #WAXABLE_BLOCK}</li>
 *     <li>{@link #STRIPPABLE_BLOCK}</li>
 *     <li>{@link #FLAMMABLE_BLOCK}</li>
 *     <li>{@link #SCULK_FREQUENCY}</li>
 * </ul>
 */
public class BlockContentRegistries {
	/**
	 * The namespace for the content registries.
	 */
	public static final String NAMESPACE = "quilt_block_content_registry";

	/**
	 * A {@link RegistryEntryAttachment} for when blocks are right clicked by a shovel.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt_block_content_registry/attachments/minecraft/block/flattenable_block.json}
	 */
	public static final RegistryEntryAttachment<Block, BlockState> FLATTENABLE_BLOCK = RegistryEntryAttachment
			.builder(Registry.BLOCK,
					new Identifier(NAMESPACE, "flattenable_block"),
					BlockState.class,
					BlockState.CODEC)
			.build();

	/**
	 * A {@link RegistryEntryAttachment} for oxidizable blocks.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt_block_content_registry/attachments/minecraft/block/oxidizable_block.json}
	 */
	public static final RegistryEntryAttachment<Block, ReversibleBlockEntry> OXIDIZABLE_BLOCK = RegistryEntryAttachment
			.builder(Registry.BLOCK,
					new Identifier(NAMESPACE, "oxidizable_block"),
					ReversibleBlockEntry.class,
					ReversibleBlockEntry.CODEC)
			.build();

	/**
	 * A {@link RegistryEntryAttachment} for waxable blocks.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt_block_content_registry/attachments/minecraft/block/waxable_block.json}
	 */
	public static final RegistryEntryAttachment<Block, ReversibleBlockEntry> WAXABLE_BLOCK = RegistryEntryAttachment
			.builder(Registry.BLOCK,
					new Identifier(NAMESPACE, "waxable_block"),
					ReversibleBlockEntry.class,
					ReversibleBlockEntry.CODEC)
			.build();

	/**
	 * A {@link RegistryEntryAttachment} for strippable blocks.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt_block_content_registry/attachments/minecraft/block/strippable_block.json}
	 */
	public static final RegistryEntryAttachment<Block, Block> STRIPPABLE_BLOCK = RegistryEntryAttachment
			.builder(Registry.BLOCK,
					new Identifier(NAMESPACE, "strippable_block"),
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
	 * Values can be set via code and through a data-pack with the file {@code data/quilt_block_content_registry/attachments/minecraft/block/flammable_block.json}
	 */
	public static final RegistryEntryAttachment<Block, FlammableBlockEntry> FLAMMABLE_BLOCK = RegistryEntryAttachment
			.builder(Registry.BLOCK,
					new Identifier(NAMESPACE, "flammable_block"),
					FlammableBlockEntry.class,
					FlammableBlockEntry.CODEC)
			.build();

	/**
	 * A {@link RegistryEntryAttachment} for sculk frequencies.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt_block_content_registry/attachments/minecraft/game_event/sculk_frequency.json}
	 */
	public static final RegistryEntryAttachment<GameEvent, Integer> SCULK_FREQUENCY = RegistryEntryAttachment
			.builder(Registry.GAME_EVENT,
					new Identifier(NAMESPACE, "sculk_frequency"),
					Integer.class,
					Codec.intRange(1, 15))
			.build();
}

