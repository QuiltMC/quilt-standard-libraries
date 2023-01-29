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

package org.quiltmc.qsl.component.test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.sync.SyncChannel;
import org.quiltmc.qsl.component.impl.container.SimpleComponentContainer;

public class TestBlockEntity extends BlockEntity {
	private final ComponentContainer container = ComponentContainer.builder(this)
	   .saving(this::markDirty)
	   .ticking()
	   .add(ComponentTestMod.TEST_BE_INT, ComponentTestMod.CHUNK_INVENTORY)
	   .syncing(SyncChannel.BLOCK_ENTITY)
	   .build(SimpleComponentContainer.FACTORY);

	private final ComponentContainer composite = ComponentContainer.createComposite(
			this.container, super.getComponentContainer());

	public TestBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(ComponentTestMod.TEST_BE_TYPE, blockPos, blockState);
	}

	public static <T extends BlockEntity> void tick(World world, BlockPos pos, BlockState ignoredState,
			T blockEntity) {
		if (world.isClient) {
			return;
		}

		blockEntity.ifPresent(ComponentTestMod.CHUNK_INVENTORY, chunkInventorySerializable -> {
			if (!chunkInventorySerializable.isEmpty()) {
				blockEntity.ifPresent(ComponentTestMod.TEST_BE_INT, integerComponent -> {
					if (integerComponent.get() % 40 == 0) {
						HashSet<BlockPos> set = new HashSet<>(List.of(pos));
						expand(pos, pos, world, set);
					}

					integerComponent.increment();
					integerComponent.save();
				});
			}
		});
	}

	private static void expand(BlockPos initialPos, BlockPos pos, World world, Set<BlockPos> visited) {
		Arrays.stream(Direction.values())
			  .map(pos::offset)
			  .filter(visited::add)
			  .forEach(offsetPos -> {
				  BlockState stateAt = world.getBlockState(offsetPos);
				  if (stateAt.isAir()) {
					  world.setBlockState(offsetPos, Blocks.DIAMOND_BLOCK.getDefaultState());
				  } else if (stateAt.isOf(Blocks.DIAMOND_BLOCK) && initialPos.isWithinDistance(offsetPos, 5)) {
					  expand(initialPos, offsetPos, world, visited);
				  }
			  });
	}

	@Override
	public ComponentContainer getComponentContainer() {
		return this.composite;
	}

	@Nullable
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.of(this);
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return this.toNbt();
	}
}
