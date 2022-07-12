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

package org.quiltmc.qsl.component.impl.client;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.Chunk;

public final class ClientResolution {
	public static BlockEntity getBlockEntity(BlockPos pos) {
		return MinecraftClient.getInstance().world.getBlockEntity(pos);
	}

	public static Entity getEntity(int id) {
		return MinecraftClient.getInstance().world.getEntityById(id);
	}

	public static Chunk getChunk(ChunkPos pos) {
		return MinecraftClient.getInstance().world.getChunk(pos.x, pos.z);
	}
}
