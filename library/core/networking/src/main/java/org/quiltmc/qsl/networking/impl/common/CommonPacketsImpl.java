/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.networking.impl.common;

import java.util.Arrays;
import java.util.function.Consumer;

import net.minecraft.network.NetworkState;
import net.minecraft.network.ServerConfigurationPacketHandler;
import net.minecraft.network.configuration.ConfigurationTask;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.server.MinecraftServer;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.networking.api.CustomPayloads;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.ServerConfigurationConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerConfigurationNetworking;
import org.quiltmc.qsl.networking.api.ServerConfigurationTaskManager;
import org.quiltmc.qsl.networking.impl.NetworkingImpl;
import org.quiltmc.qsl.networking.impl.server.ServerConfigurationNetworkAddon;
import org.quiltmc.qsl.networking.impl.server.ServerNetworkingImpl;

public class CommonPacketsImpl {
	public static final int PACKET_VERSION_1 = 1;
	public static final int[] SUPPORTED_COMMON_PACKET_VERSIONS = new int[]{PACKET_VERSION_1};

	public static void init(ModContainer mod) {
		CustomPayloads.registerC2SPayload(CommonVersionPayload.PACKET_ID, CommonVersionPayload::new);
		CustomPayloads.registerC2SPayload(CommonRegisterPayload.PACKET_ID, CommonRegisterPayload::new);

		ServerConfigurationNetworking.registerGlobalReceiver(CommonVersionPayload.PACKET_ID, CommonPacketsImpl::handleCommonVersion);
		ServerConfigurationNetworking.registerGlobalReceiver(CommonRegisterPayload.PACKET_ID, CommonPacketsImpl::handleCommonRegister);

		// Create a configuration task to send and receive the common packets
		ServerConfigurationConnectionEvents.ADD_TASKS.register((handler, server) -> {
			final ServerConfigurationNetworkAddon addon = ServerNetworkingImpl.getAddon(handler);

			if (ServerConfigurationNetworking.canSend(handler, CommonVersionPayload.PACKET_ID)) {
				// Tasks are processed in order.
				((ServerConfigurationTaskManager) handler).addTask(new CommonVersionConfigurationTask(addon));

				if (ServerConfigurationNetworking.canSend(handler, CommonRegisterPayload.PACKET_ID)) {
					((ServerConfigurationTaskManager) handler).addTask(new CommonRegisterConfigurationTask(addon));
				}
			}
		});
	}

	private static void handleCommonVersion(MinecraftServer server, ServerConfigurationPacketHandler handler, CommonVersionPayload payload, PacketSender<CustomPayload> responseSender) {
		ServerConfigurationNetworkAddon addon = ServerNetworkingImpl.getAddon(handler);
		addon.onCommonVersionPacket(getNegotiatedVersion(payload));
		((ServerConfigurationTaskManager) handler).finishTask(CommonVersionConfigurationTask.KEY);
	}

	private static void handleCommonRegister(MinecraftServer server, ServerConfigurationPacketHandler handler, CommonRegisterPayload payload, PacketSender<CustomPayload> responseSender) {
		ServerConfigurationNetworkAddon addon = ServerNetworkingImpl.getAddon(handler);

		if (CommonRegisterPayload.PLAY_PHASE.equals(payload.phase())) {
			if (payload.version() != addon.getNegotiatedVersion()) {
				throw new IllegalStateException("Negotiated common packet version: %d but received packet with version: %d".formatted(addon.getNegotiatedVersion(), payload.version()));
			}

			// Play phase hasnt started yet, add them to the pending names.
			addon.getChannelInfoHolder().getPendingChannelsNames(NetworkState.PLAY).addAll(payload.channels());
			NetworkingImpl.LOGGER.debug("Received accepted channels from the client for play phase");
		} else {
			addon.onCommonRegisterPacket(payload);
		}

		((ServerConfigurationTaskManager) handler).finishTask(CommonRegisterConfigurationTask.KEY);
	}

	// A configuration phase task to send and receive the version packets.
	private record CommonVersionConfigurationTask(ServerConfigurationNetworkAddon addon) implements ConfigurationTask {
		public static final Type KEY = new Type(CommonVersionPayload.PACKET_ID.toString());

		@Override
		public void start(Consumer<Packet<?>> sender) {
			this.addon.sendPayload(new CommonVersionPayload(SUPPORTED_COMMON_PACKET_VERSIONS));
		}

		@Override
		public Type getType() {
			return KEY;
		}
	}

	// A configuration phase task to send and receive the registration packets.
	private record CommonRegisterConfigurationTask(ServerConfigurationNetworkAddon addon) implements ConfigurationTask {
		public static final Type KEY = new Type(CommonRegisterPayload.PACKET_ID.toString());

		@Override
		public void start(Consumer<Packet<?>> sender) {
			this.addon.sendPayload(this.addon.createRegisterPayload());
		}

		@Override
		public Type getType() {
			return KEY;
		}
	}

	private static int getNegotiatedVersion(CommonVersionPayload payload) {
		int version = getHighestCommonVersion(payload.versions(), SUPPORTED_COMMON_PACKET_VERSIONS);

		if (version <= 0) {
			throw new UnsupportedOperationException("Server does not support any requested versions from client");
		}

		return version;
	}

	public static int getHighestCommonVersion(int[] a, int[] b) {
		int[] as = a.clone();
		int[] bs = b.clone();

		Arrays.sort(as);
		Arrays.sort(bs);

		int ap = as.length - 1;
		int bp = bs.length - 1;

		while (ap >= 0 && bp >= 0) {
			if (as[ap] == bs[bp]) {
				return as[ap];
			}

			if (as[ap] > bs[bp]) {
				ap--;
			} else {
				bp--;
			}
		}

		return -1;
	}
}
