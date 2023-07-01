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

package org.quiltmc.qsl.block.entity.test;

import java.util.Random;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;

import org.quiltmc.qsl.block.entity.api.QuiltBlockEntity;

public class ColorfulBlockEntity extends BlockEntity implements QuiltBlockEntity {
	private static final Random RANDOM = new Random();
	private int color = RANDOM.nextInt(0xffffff + 1);

	public ColorfulBlockEntity(BlockPos blockPos, BlockState blockState) {
		super(BlockEntityTypeTest.COLORFUL_BLOCK_ENTITY_TYPE, blockPos, blockState);
	}

	public void rollColor() {
		this.color = RANDOM.nextInt(0xffffff + 1);

		if (this.world != null && !this.world.isClient()) {
			this.markDirty();
			this.sync();
		}
	}

	public int getColor() {
		return this.color;
	}

	/* Serialization */

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);

		try {
			this.color = Integer.parseInt(nbt.getString("color"), 16);
		} catch (NumberFormatException e) {
			this.color = 0;
		}

		if (this.world != null && this.world.isClient()) {
			this.refreshRendering();
		}
	}

	public void refreshRendering() {
		if (this.world instanceof ClientWorld clientWorld) {
			clientWorld.scheduleBlockRenders(
					ChunkSectionPos.getSectionCoord(this.getPos().getX()),
					ChunkSectionPos.getSectionCoord(this.getPos().getY()),
					ChunkSectionPos.getSectionCoord(this.getPos().getZ())
			);
		}
	}

	@Override
	public void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		nbt.putString("color", Integer.toHexString(this.color));
	}

	@Override
	public NbtCompound toSyncedNbt() {
		return this.toNbt();
	}

	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.of(this);
	}
}
