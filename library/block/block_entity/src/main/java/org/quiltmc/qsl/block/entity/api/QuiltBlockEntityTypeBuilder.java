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

package org.quiltmc.qsl.block.entity.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.datafixers.types.Type;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;

/**
 * Provides a way to build {@link BlockEntityType} with more features than {@link BlockEntityType.Builder}.
 *
 * @param <BE> the block entity Java type
 */
public final class QuiltBlockEntityTypeBuilder<BE extends BlockEntity> {
	private final BlockEntityType.BlockEntityFactory<? extends BE> factory;
	private final List<Block> supportedBlocks;

	private QuiltBlockEntityTypeBuilder(BlockEntityType.BlockEntityFactory<? extends BE> factory, List<Block> supportedBlocks) {
		this.factory = factory;
		this.supportedBlocks = supportedBlocks;
	}

	/**
	 * Creates a new block entity type builder from a block entity factory and an initial array of supported blocks.
	 *
	 * @param factory         the block entity factory
	 * @param supportedBlocks the initial array of supported blocks
	 * @param <BE>            the block entity Java type
	 * @return a new block entity type builder
	 */
	public static <BE extends BlockEntity> QuiltBlockEntityTypeBuilder<BE> create(BlockEntityType.BlockEntityFactory<? extends BE> factory,
			Block... supportedBlocks) {
		var blocks = new ArrayList<Block>(supportedBlocks.length);
		Collections.addAll(blocks, supportedBlocks);

		return new QuiltBlockEntityTypeBuilder<>(factory, blocks);
	}

	/**
	 * Adds a supported block to the block entity type which is being built.
	 *
	 * @param block the supported block
	 * @return this builder
	 */
	public QuiltBlockEntityTypeBuilder<BE> addBlock(Block block) {
		this.supportedBlocks.add(block);
		return this;
	}

	/**
	 * Adds supported blocks to this block entity type which is being built.
	 *
	 * @param blocks the supported blocks
	 * @return this builder
	 */
	public QuiltBlockEntityTypeBuilder<BE> addBlocks(Block... blocks) {
		Collections.addAll(this.supportedBlocks, blocks);
		return this;
	}

	/**
	 * Builds the block entity type.
	 *
	 * @param type the DFU type, this may be used for datafixers
	 * @return the built block entity type
	 * @see #build() build without any care for datafixers
	 */
	public BlockEntityType<BE> build(@Nullable Type<?> type) {
		return BlockEntityType.Builder.<BE>create(this.factory, this.supportedBlocks.toArray(Block[]::new))
				.build(type);
	}

	/**
	 * Builds the block entity type.
	 *
	 * @return the built block entity type
	 */
	public BlockEntityType<BE> build() {
		return this.build(null);
	}
}
