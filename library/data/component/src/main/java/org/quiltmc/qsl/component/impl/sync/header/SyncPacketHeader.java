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
import net.minecraft.world.chunk.Chunk;
import org.quiltmc.qsl.base.api.util.Maybe;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.CommonInitializer;
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

	// The default implemented types, that can sync.
	public static final SyncPacketHeader<BlockEntity> BLOCK_ENTITY = new SyncPacketHeader<>(NetworkCodec.BLOCK_ENTITY);
	public static final SyncPacketHeader<Entity> ENTITY = new SyncPacketHeader<>(NetworkCodec.ENTITY);
	public static final SyncPacketHeader<Chunk> CHUNK = new SyncPacketHeader<>(NetworkCodec.CHUNK);
	private static final NetworkCodec<ComponentProvider> LEVEL_CODEC = new NetworkCodec<>(
			(buf, provider) -> {
			},
			buf -> MinecraftClient.getInstance()
	);
	public static final SyncPacketHeader<?> SAVE = new SyncPacketHeader<>(LEVEL_CODEC);

	public static void registerDefaults() {
		register(CommonInitializer.id("block_entity"), BLOCK_ENTITY);
		register(CommonInitializer.id("entity"), ENTITY);
		register(CommonInitializer.id("chunk"), CHUNK);
		register(CommonInitializer.id("level"), SAVE);
	}

	public static <P extends ComponentProvider> void register(Identifier id, SyncPacketHeader<P> header) {
		Registry.register(REGISTRY, id, header);
	}

	public static Maybe<? extends ComponentProvider> toProvider(PacketByteBuf buf) {
		return NETWORK_CODEC.decode(buf)
				.map(SyncPacketHeader::codec)
				.filterMap(networkCodec -> networkCodec.decode(buf));
	}

	@SuppressWarnings("unchecked")
	public PacketByteBuf start(ComponentProvider provider) {
		var buf = PacketByteBufs.create();
		buf.writeInt(REGISTRY.getRawId(this));
		// the person calling is responsible to make sure we get a valid provider instance!
		this.codec.encode(buf, (P) provider);
		return buf;
	}
}
