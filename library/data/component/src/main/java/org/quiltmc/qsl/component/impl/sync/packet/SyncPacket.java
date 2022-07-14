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
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.component.SyncedComponent;
import org.quiltmc.qsl.component.impl.sync.header.SyncPacketHeader;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

import java.util.Collection;
import java.util.Queue;
import java.util.function.Function;
import java.util.function.Supplier;

public class SyncPacket {
	/**
	 * <pre>
 *     		Handled by SyncPacketHeader		The data we add
	 *        HEADER_ID PROVIDER_DATA       SIZE [TYPE DATA]
	 *        	32bit		var				32bit	 var
	 * </pre>
	 */
	public static void handle(PacketByteBuf buf, MinecraftClient client) {
		buf.retain(); // We need the buffer to exist until the client next pulls tasks on the main thread!

		client.execute(() -> {
			SyncPacketHeader.fromBuffer(buf).ifJust(provider -> {
				var size = buf.readInt();

				for (int i = 0; i < size; i++) {
					ComponentType.NETWORK_CODEC.decode(buf)
							.filterMap(provider::expose)
							.map(it -> ((SyncedComponent) it))
							.ifJust(synced -> synced.readFromBuf(buf));
				}
			});

			buf.release(); // Make sure the buffer is cleared!
		});
	}

	public static void send(SyncContext context, PacketByteBuf packet) {
		context.playerGenerator().get().forEach(serverPlayer ->
				ServerPlayNetworking.send(serverPlayer, PacketIds.SYNC, packet)
		);
	}

	public static void syncFromQueue(
			Queue<ComponentType<?>> pendingSync,
			SyncPacket.SyncContext context,
			Function<ComponentType<?>, SyncedComponent> mapper,
			ComponentProvider provider
	) {
		if (pendingSync.isEmpty()) {
			return;
		}

		PacketByteBuf buf = context.header().toBuffer(provider);
		buf.writeInt(pendingSync.size());

		while (!pendingSync.isEmpty()) {
			var currentType = pendingSync.poll();
			ComponentType.NETWORK_CODEC.encode(buf, currentType);
			mapper.apply(currentType).writeToBuf(buf);
		}

		SyncPacket.send(context, buf);
	}

	public record SyncContext(SyncPacketHeader<?> header, Supplier<Collection<ServerPlayerEntity>> playerGenerator) {
	}
}
