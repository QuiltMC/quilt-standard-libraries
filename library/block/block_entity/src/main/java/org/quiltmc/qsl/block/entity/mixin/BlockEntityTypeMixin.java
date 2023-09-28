/*
 * Copyright 2022-2023 QuiltMC
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

package org.quiltmc.qsl.block.entity.mixin;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntityType;

import org.quiltmc.qsl.block.entity.api.QuiltBlockEntityType;
import org.quiltmc.qsl.block.entity.impl.QuiltBlockEntityImpl;

@Mixin(BlockEntityType.class)
public class BlockEntityTypeMixin implements QuiltBlockEntityType {
	@Mutable
	@Shadow
	@Final
	private Set<Block> blocks;

	@Unique
	public Set<Block> quilt$getMutableSupportedBlocks() {
		if (this.blocks instanceof ImmutableSet) {
			this.blocks = new HashSet<>(this.blocks);
		}

		return this.blocks;
	}

	@Override
	public void addSupportedBlock(Block block) {
		QuiltBlockEntityImpl.INSTANCE.ensureCanModify();
		this.quilt$getMutableSupportedBlocks().add(block);
	}

	@Override
	public void addSupportedBlocks(Block... blocks) {
		QuiltBlockEntityImpl.INSTANCE.ensureCanModify();
		Collections.addAll(this.quilt$getMutableSupportedBlocks(), blocks);
	}
}
