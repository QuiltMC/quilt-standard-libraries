/*
 * Copyright 2025 The Quilt Project
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

package org.quiltmc.qsl.block.entity.mixin.accessor;

import java.util.Set;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.block.Block;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityType;

@Mixin(BlockEntityType.class)
public interface BlockEntityTypeAccessor {
	@Invoker("<init>")
	static <T extends BlockEntity> BlockEntityType<T> quilt$create(
			BlockEntityType.BlockEntityFactory<? extends T> factory, Set<Block> blocks
	) {
		throw new AssertionError("dummy method body invoked");
	}
}
