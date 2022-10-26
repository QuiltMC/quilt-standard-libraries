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


import com.mojang.serialization.DataResult;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;

/**
 * Holds {@link RegistryEntryAttachment}s for different properties that blocks can hold.
 * <p>
 * Current properties:
 * <ul>
 *     <li>{@link #FLATTENABLES}</li>
 *     <li>{@link #OXIDIZABLES}</li>
 *     <li>{@link #WAXABLES}</li>
 *     <li>{@link #STRIPPABLES}</li>
 *     <li>{@link #FLAMMABLES}</li>
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
	 * Values can be set via code and through a data-pack with the file {@code data/quilt/attachments/minecraft/block/flattenables.json}
	 */
	public static final RegistryEntryAttachment<Block, BlockState> FLATTENABLES = RegistryEntryAttachment
			.builder(Registry.BLOCK,
					new Identifier(NAMESPACE, "flattenables"),
					BlockState.class,
					BlockState.CODEC)
			.build();

	/**
	 * A {@link RegistryEntryAttachment} for oxidizable blocks.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt/attachments/minecraft/block/oxidizables.json}
	 */
	public static final RegistryEntryAttachment<Block, ReversibleBlockEntry> OXIDIZABLES = RegistryEntryAttachment
			.builder(Registry.BLOCK,
					new Identifier(NAMESPACE, "oxidizables"),
					ReversibleBlockEntry.class,
					ReversibleBlockEntry.CODEC)
			.build();

	/**
	 * A {@link RegistryEntryAttachment} for waxable blocks.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt/attachments/minecraft/block/waxables.json}
	 */
	public static final RegistryEntryAttachment<Block, ReversibleBlockEntry> WAXABLES = RegistryEntryAttachment
			.builder(Registry.BLOCK,
					new Identifier(NAMESPACE, "waxables"),
					ReversibleBlockEntry.class,
					ReversibleBlockEntry.CODEC)
			.build();

	/**
	 * A {@link RegistryEntryAttachment} for strippable blocks.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt/attachments/minecraft/block/strippables.json}
	 */
	public static final RegistryEntryAttachment<Block, Block> STRIPPABLES = RegistryEntryAttachment
			.builder(Registry.BLOCK,
					new Identifier(NAMESPACE, "strippables"),
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
	 * Values can be set via code and through a data-pack with the file {@code data/quilt/attachments/minecraft/block/flammables.json}
	 */
	public static final RegistryEntryAttachment<Block, FlammableBlockEntry> FLAMMABLES = RegistryEntryAttachment
			.builder(Registry.BLOCK,
					new Identifier(NAMESPACE, "flammables"),
					FlammableBlockEntry.class,
					FlammableBlockEntry.CODEC)
			.build();
}

