package org.quiltmc.qsl.component.impl.sync;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.networking.api.PlayerLookup;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class SyncPlayerList {
	@NotNull
	public static Collection<ServerPlayerEntity> create(World world, BlockPos pos) {
		return world.isClient ? List.of() : PlayerLookup.tracking((ServerWorld) world, pos);
	}

	@NotNull
	public static Collection<ServerPlayerEntity> create(Entity entity) {
		return entity.getWorld().isClient ? List.of() : PlayerLookup.tracking(entity);
	}

	@NotNull
	public static Collection<ServerPlayerEntity> create(BlockEntity blockEntity) {
		return Objects.requireNonNull(blockEntity.getWorld()).isClient ? List.of() : PlayerLookup.tracking(blockEntity);
	}

	@NotNull
	public static Collection<ServerPlayerEntity> create(WorldChunk chunk) {
		return chunk.getWorld().isClient ? List.of() : PlayerLookup.tracking((ServerWorld) chunk.getWorld(), chunk.getPos());
	}
}
