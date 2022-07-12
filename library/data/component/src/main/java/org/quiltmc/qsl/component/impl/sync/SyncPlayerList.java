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

package org.quiltmc.qsl.component.impl.sync;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.quiltmc.qsl.networking.api.PlayerLookup;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public final class SyncPlayerList {
	public static Collection<ServerPlayerEntity> create(World world, BlockPos pos) {
		return world.isClient ? List.of() : PlayerLookup.tracking((ServerWorld) world, pos);
	}

	public static Collection<ServerPlayerEntity> create(Entity entity) {
		return entity.getWorld().isClient ? List.of() : PlayerLookup.tracking(entity);
	}

	public static Collection<ServerPlayerEntity> create(BlockEntity blockEntity) {
		return Objects.requireNonNull(blockEntity.getWorld()).isClient ? List.of() : PlayerLookup.tracking(blockEntity);
	}

	public static Collection<ServerPlayerEntity> create(WorldChunk chunk) {
		return chunk.getWorld().isClient ? List.of() : PlayerLookup.tracking((ServerWorld) chunk.getWorld(), chunk.getPos());
	}

	public static Collection<ServerPlayerEntity> create(ServerWorld world) {
		return PlayerLookup.world(world);
	}

	public static Collection<ServerPlayerEntity> create(World world) {
		return world.isClient ? List.of() : PlayerLookup.world((ServerWorld) world);
	}
}
