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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.api.ComponentType;
import org.quiltmc.qsl.component.api.component.SyncedComponent;
import org.quiltmc.qsl.component.impl.sync.SyncChannel;

import java.util.Queue;
import java.util.function.Function;

/**
 * <pre>
 *         PROVIDER_DATA  SIZE [TYPE DATA]
 *             var		  32bit	   var
 * </pre>
 */
public class SyncPacket {
	public static <P extends ComponentProvider> void createFromQueue(
			Queue<ComponentType<?>> pendingSync,
			SyncChannel<P> channel,
			Function<ComponentType<?>, SyncedComponent> mapper,
			ComponentProvider provider
	) {
		if (pendingSync.isEmpty()) {
			return;
		}

		channel.send(provider, buf -> {
			buf.writeInt(pendingSync.size()); // append size

			while (!pendingSync.isEmpty()) {
				var currentType = pendingSync.poll();
				ComponentType.NETWORK_CODEC.encode(buf, currentType); // append type rawId
				mapper.apply(currentType).writeToBuf(buf); // append component data
			}
		});
	}

	@Environment(EnvType.CLIENT)
	public static void handle(MinecraftClient client, ComponentProvider provider, PacketByteBuf buf) {
		buf.retain(); // hold the buffer in memory

		client.execute(() -> {
			int size = buf.readInt(); // consume size

			for (int i = 0; i < size; i++) {
				ComponentType.NETWORK_CODEC.decode(buf) // consume type rawId
						.filterMap(provider::expose)
						.map(it -> ((SyncedComponent) it))
						.ifJust(syncedComponent -> syncedComponent.readFromBuf(buf)); // consume data
			}

			buf.release(); // make sure the buffer is freed now that we don't need it
		});
	}
}
