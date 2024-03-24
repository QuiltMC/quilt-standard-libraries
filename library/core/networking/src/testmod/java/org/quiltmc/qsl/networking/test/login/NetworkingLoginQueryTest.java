/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.networking.test.login;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.FutureTask;

import net.minecraft.network.packet.s2c.login.payload.CustomQueryPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerLoginConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerLoginNetworking;
import org.quiltmc.qsl.networking.test.NetworkingTestMods;

public final class NetworkingLoginQueryTest implements ModInitializer {
	public static final Identifier TEST_CHANNEL_GLOBAL = NetworkingTestMods.id("test_channel_global");
	public static final Identifier TEST_CHANNEL = NetworkingTestMods.id("test_channel");
	private static final boolean useLoginDelayTest = System.getProperty("quilt_networking.login_delay_test") != null;

	@Override
	public void onInitialize(ModContainer mod) {
		ServerLoginConnectionEvents.QUERY_START.register(this::onLoginStart);
		ServerLoginConnectionEvents.QUERY_START.register(this::delaySimply);

		// login delaying example
		ServerLoginNetworking.registerGlobalReceiver(TEST_CHANNEL_GLOBAL, (server, handler, understood, buf, synchronizer, sender) -> {
			if (understood) {
				NetworkingTestMods.LOGGER.info("Understood response from client in {}", TEST_CHANNEL_GLOBAL);

				if (useLoginDelayTest) {
					FutureTask<?> future = new FutureTask<>(() -> {
						for (int i = 0; i <= 10; i++) {
							Thread.sleep(300);
							NetworkingTestMods.LOGGER.info("Delayed login for number {} 300 milliseconds", i);
						}

						return null;
					});

					// Execute the task on a worker thread as not to block the server thread
					Util.getMainWorkerExecutor().execute(future);
					synchronizer.waitFor(future);
				}
			} else {
				NetworkingTestMods.LOGGER.info("Client did not understand response query message with channel name {}", TEST_CHANNEL_GLOBAL);
			}
		});
	}

	private void delaySimply(ServerLoginNetworkHandler handler, MinecraftServer server, PacketSender<CustomQueryPayload> sender, ServerLoginNetworking.LoginSynchronizer synchronizer) {
		if (useLoginDelayTest) {
			synchronizer.waitFor(CompletableFuture.runAsync(() -> {
				NetworkingTestMods.LOGGER.info("Starting simple delay task for 3000 milliseconds");

				try {
					Thread.sleep(3000);
					NetworkingTestMods.LOGGER.info("Simple delay task completed");
				} catch (InterruptedException e) {
					NetworkingTestMods.LOGGER.error("Delay task caught exception", e);
				}
			}));
		}
	}

	private void onLoginStart(ServerLoginNetworkHandler networkHandler, MinecraftServer server, PacketSender<CustomQueryPayload> sender, ServerLoginNetworking.LoginSynchronizer synchronizer) {
		NetworkingTestMods.LOGGER.info("Query Start event received.");

		ServerLoginNetworking.registerReceiver(networkHandler, TEST_CHANNEL, (_server, _handler, understood, buf, _synchronizer, _sender) -> {
			if (understood) {
				NetworkingTestMods.LOGGER.info("Understood response from client in {}", TEST_CHANNEL);
			} else {
				NetworkingTestMods.LOGGER.info("Client did not understand response query message with channel name {}", TEST_CHANNEL);
			}
		});

		// Send a dummy query when the client starts accepting queries.
		sender.sendPacket(TEST_CHANNEL_GLOBAL, PacketByteBufs.empty()); // dummy packet
		sender.sendPacket(TEST_CHANNEL, PacketByteBufs.empty()); // dummy packet
	}
}
