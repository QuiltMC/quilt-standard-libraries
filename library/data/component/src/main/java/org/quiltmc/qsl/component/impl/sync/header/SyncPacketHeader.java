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

package org.quiltmc.qsl.component.impl.sync.header;

import com.mojang.serialization.Lifecycle;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.CommonInitializer;
import org.quiltmc.qsl.component.impl.client.ClientResolution;
import org.quiltmc.qsl.component.impl.client.sync.ClientSyncHandler;
import org.quiltmc.qsl.component.impl.sync.codec.NetworkCodec;
import org.quiltmc.qsl.networking.api.PacketByteBufs;

public record SyncPacketHeader<P extends ComponentProvider>(NetworkCodec<P> codec) {
	// Registry TODO: Maybe register this?!
	public static final RegistryKey<Registry<SyncPacketHeader<?>>> REGISTRY_KEY =
			RegistryKey.ofRegistry(CommonInitializer.id("sync_headers"));
	public static final Registry<SyncPacketHeader<?>> REGISTRY =
			new SimpleRegistry<>(REGISTRY_KEY, Lifecycle.experimental(), null);

	// Codec
	public static final NetworkCodec<SyncPacketHeader<?>> NETWORK_CODEC =
			NetworkCodec.INT.map(REGISTRY::getRawId, ClientSyncHandler.getInstance()::getHeader);

	// BlockEntity
	public static final NetworkCodec<BlockEntity> BLOCK_ENTITY_CODEC =
			NetworkCodec.BLOCK_POS.map(BlockEntity::getPos, ClientResolution::getBlockEntity);
	public static final SyncPacketHeader<BlockEntity> BLOCK_ENTITY = register("block_entity", BLOCK_ENTITY_CODEC);


	// Entity
	public static final NetworkCodec<Entity> ENTITY_CODEC =
			NetworkCodec.INT.map(Entity::getId, ClientResolution::getEntity);
	public static final SyncPacketHeader<Entity> ENTITY = register("entity", ENTITY_CODEC);

	// Chunk
	public static final NetworkCodec<Chunk> CHUNK_CODEC =
			NetworkCodec.CHUNK_POS.map(Chunk::getPos, ClientResolution::getChunk);
	public static final SyncPacketHeader<Chunk> CHUNK = register("chunk", CHUNK_CODEC);

	// World
	public static final NetworkCodec<World> WORLD_CODEC = NetworkCodec.empty(() -> MinecraftClient.getInstance().world);
	public static final SyncPacketHeader<World> WORLD = register("world", WORLD_CODEC);

	// Level aka Save
	private static final NetworkCodec<ComponentProvider> LEVEL_CODEC = NetworkCodec.empty(MinecraftClient::getInstance);
	public static final SyncPacketHeader<?> LEVEL = register("level", LEVEL_CODEC);

	public static void registerDefaults() {
		// Only exists to make sure these fields are classloaded before registries are frozen.
	}

	private static <P extends ComponentProvider> SyncPacketHeader<P> register(String id, NetworkCodec<P> codec) {
		SyncPacketHeader<P> header = new SyncPacketHeader<>(codec);
		register(CommonInitializer.id(id), header);
		return header;
	}

	public static <P extends ComponentProvider> void register(Identifier id, SyncPacketHeader<P> header) {
		Registry.register(REGISTRY, id, header);
	}

	public static Maybe<? extends ComponentProvider> fromBuffer(PacketByteBuf buf) {
		return NETWORK_CODEC.decode(buf)
				.map(SyncPacketHeader::codec)
				.filterMap(networkCodec -> networkCodec.decode(buf));
	}

	@SuppressWarnings("unchecked")
	public PacketByteBuf toBuffer(ComponentProvider provider) {
		var buf = PacketByteBufs.create();
		buf.writeInt(REGISTRY.getRawId(this));
		// the person calling is responsible to make sure we get a valid provider instance!
		this.codec.encode(buf, (P) provider);
		return buf;
	}
}
