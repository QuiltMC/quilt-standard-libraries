package org.quiltmc.qsl.component.impl.client.sync;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import org.quiltmc.qsl.component.api.provider.ComponentProvider;

// all of these are called after the client joins a world, so no NPEs will be thrown
@SuppressWarnings("ConstantConditions")
@Environment(EnvType.CLIENT)
public final class ClientResolution {
	public static Entity entity(int id) {
		return MinecraftClient.getInstance().world.getEntityById(id);
	}

	public static BlockEntity blockEntity(BlockPos pos) {
		return MinecraftClient.getInstance().world.getBlockEntity(pos);
	}

	public static Chunk chunk(ChunkPos pos) {
		return MinecraftClient.getInstance().world.getChunk(pos.x, pos.z);
	}

	public static World world() {
		return MinecraftClient.getInstance().world;
	}

	public static ComponentProvider level() {
		return MinecraftClient.getInstance();
	}

	public static Chunk chunk(int chunkX, int chunkZ) {
		return MinecraftClient.getInstance().world.getChunk(chunkX, chunkZ);
	}
}
