/*
 * Copyright 2024 The Quilt Project
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

package org.quiltmc.qsl.networking.test.payload;

import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.PayloadTypeRegistry;
import org.quiltmc.qsl.networking.api.server.ServerPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.networking.test.NetworkingTestMods;

// Test cannot run on a server, so only run with a client
public class S2CPayloadTest implements ClientModInitializer {
	private static boolean received = false;

	@Override
	public void onInitializeClient(ModContainer mod) {
		PayloadTypeRegistry.playS2C().register(TestS2CPayload.ID, TestS2CPayload.CODEC);
		ClientPlayNetworking.registerGlobalReceiver(TestS2CPayload.ID, this::handleTestPayload);

		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			sender.sendPayload(new TestS2CPayload(List.of("String"), 1, 1.0));

			server.execute(() -> {
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					throw new RuntimeException(e);
				}

				if (!received) {
					throw new IllegalStateException("Did not receive S2C payload on the client.");
				}
			});
		});
	}

	public void handleTestPayload(MinecraftClient client, ClientPlayNetworkHandler handler, TestS2CPayload payload, PacketSender<CustomPayload> responseSender) {
		NetworkingTestMods.LOGGER.info("Received S2C payload: {}", payload.toString());
		received = true;
	}

	public record TestS2CPayload(List<String> strings, int a, double b) implements CustomPayload {
		public static final Id<TestS2CPayload> ID = new Id<>(Identifier.of("quilt_networking_testmod", "test_s2c_payload"));
		public static final PacketCodec<PacketByteBuf, TestS2CPayload> CODEC = CustomPayload.create(TestS2CPayload::write, TestS2CPayload::new);

		TestS2CPayload(PacketByteBuf buf) {
			this(buf.readList(PacketByteBuf::readString), buf.readInt(), buf.readDouble());
		}

		private void write(PacketByteBuf buf) {
			buf.writeCollection(this.strings, PacketByteBuf::writeString);
			buf.writeInt(this.a);
			buf.writeDouble(this.b);
		}

		@Override
		public Id<? extends CustomPayload> getId() {
			return ID;
		}
	}
}
