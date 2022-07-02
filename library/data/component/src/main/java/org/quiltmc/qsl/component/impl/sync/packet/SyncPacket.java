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

package org.quiltmc.qsl.component.impl.sync.packet;

import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.component.api.ComponentProvider;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.components.SyncedComponent;
import org.quiltmc.qsl.component.impl.client.sync.ClientSyncHandler;
import org.quiltmc.qsl.component.impl.sync.codec.NetworkCodec;
import org.quiltmc.qsl.component.impl.sync.header.SyncPacketHeader;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import java.util.Collection;
import java.util.Map;
import java.util.function.Supplier;

public class SyncPacket {
	@NotNull
	public static PacketByteBuf create(SyncPacketHeader<?> headerCreator, @NotNull ComponentProvider provider, Map<ComponentType<?>, SyncedComponent> components) {
		PacketByteBuf buff = headerCreator.start(provider);
		buff.writeInt(components.size());
		components.forEach((type, syncedComponent) -> {
			NetworkCodec.COMPONENT_TYPE.encode(buff, type);
			syncedComponent.writeToBuf(buff);
		});

		return buff;
	}

	public static void handle(PacketByteBuf buf, MinecraftClient client) {
		buf.retain();

		client.execute(() -> {
			var header = ClientSyncHandler.getInstance().getHeader(buf.readInt());
			header.codec().decode(buf).ifPresent(provider -> {
				var size = buf.readInt();

				for (int i = 0; i < size; i++) {
					NetworkCodec.COMPONENT_TYPE.decode(buf)
							.ifPresent(type -> provider.getComponentContainer().receiveSyncPacket(type, buf));
				}
			});
			buf.release();
		});
	}

	public static void send(SyncContext context, ComponentProvider provider, Map<ComponentType<?>, SyncedComponent> map) {
		PacketByteBuf packet = create(context.header(), provider, map);

		context.playerGenerator().get().forEach(serverPlayer ->
				ServerPlayNetworking.send(serverPlayer, PacketIds.SYNC, packet)
		);
	}

	public record SyncContext(SyncPacketHeader<?> header, Supplier<Collection<ServerPlayerEntity>> playerGenerator) {
	}
}
