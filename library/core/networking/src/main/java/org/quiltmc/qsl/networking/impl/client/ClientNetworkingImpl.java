/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.networking.impl.client;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.payload.CustomPayload;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.PayloadTypeRegistry;
import org.quiltmc.qsl.networking.api.client.ClientConfigurationConnectionEvents;
import org.quiltmc.qsl.networking.api.client.ClientConfigurationNetworking;
import org.quiltmc.qsl.networking.api.client.ClientLoginNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.networking.impl.GlobalReceiverRegistry;
import org.quiltmc.qsl.networking.impl.NetworkHandlerExtensions;
import org.quiltmc.qsl.networking.impl.NetworkingImpl;
import org.quiltmc.qsl.networking.impl.PayloadTypeRegistryImpl;
import org.quiltmc.qsl.networking.impl.common.CommonPacketsImpl;
import org.quiltmc.qsl.networking.impl.common.CommonRegisterPayload;
import org.quiltmc.qsl.networking.impl.common.CommonVersionPayload;
import org.quiltmc.qsl.networking.mixin.accessor.ConnectScreenAccessor;
import org.quiltmc.qsl.networking.mixin.accessor.MinecraftClientAccessor;

@ApiStatus.Internal
@ClientOnly
public final class ClientNetworkingImpl {
	public static final GlobalReceiverRegistry<ClientLoginNetworking.QueryRequestReceiver> LOGIN = new GlobalReceiverRegistry<>(NetworkSide.S2C, NetworkPhase.LOGIN, null);
	public static final GlobalReceiverRegistry<ClientConfigurationNetworking.CustomChannelReceiver<?>> CONFIGURATION = new GlobalReceiverRegistry<>(NetworkSide.S2C, NetworkPhase.CONFIGURATION, PayloadTypeRegistryImpl.CONFIGURATION_S2C);
	public static final GlobalReceiverRegistry<ClientPlayNetworking.CustomChannelReceiver<?>> PLAY = new GlobalReceiverRegistry<>(NetworkSide.S2C, NetworkPhase.PLAY, PayloadTypeRegistryImpl.PLAY_S2C);
	private static ClientPlayNetworkAddon currentPlayAddon;
	private static ClientConfigurationNetworkAddon currentConfigurationAddon;

	public static ClientPlayNetworkAddon getAddon(ClientPlayNetworkHandler handler) {
		return (ClientPlayNetworkAddon) ((NetworkHandlerExtensions) handler).getAddon();
	}

	public static ClientConfigurationNetworkAddon getAddon(ClientConfigurationNetworkHandler handler) {
		return (ClientConfigurationNetworkAddon) ((NetworkHandlerExtensions) handler).getAddon();
	}

	public static ClientLoginNetworkAddon getAddon(ClientLoginNetworkHandler handler) {
		return (ClientLoginNetworkAddon) ((NetworkHandlerExtensions) handler).getAddon();
	}

	public static Packet<ServerCommonPacketListener> createC2SPacket(CustomPayload payload) {
		return new CustomPayloadC2SPacket(payload);
	}

	/**
	 * Due to the way logging into a integrated or remote dedicated server will differ, we need to obtain the login client connection differently.
	 */
	public static @Nullable ClientConnection getLoginConnection() {
		final ClientConnection connection = ((MinecraftClientAccessor) MinecraftClient.getInstance()).getConnection();

		// Check if we are connecting to an integrated server. This will set the field on MinecraftClient
		if (connection != null) {
			return connection;
		} else {
			// We are probably connecting to a remote server.
			// Check if the ConnectScreen is the currentScreen to determine that:
			if (MinecraftClient.getInstance().currentScreen instanceof ConnectScreen) {
				return ((ConnectScreenAccessor) MinecraftClient.getInstance().currentScreen).getConnection();
			}
		}

		// We are not connected to a server at all.
		return null;
	}

	public static @Nullable ClientConfigurationNetworkAddon getClientConfigurationAddon() {
		return currentConfigurationAddon;
	}

	public static @Nullable ClientPlayNetworkAddon getClientPlayAddon() {
		// Since Minecraft can be a bit weird, we need to check for the play addon in a few ways:
		// If the client's player is set this will work
		if (MinecraftClient.getInstance().getNetworkHandler() != null) {
			currentPlayAddon = null; // Shouldn't need this anymore
			return ClientNetworkingImpl.getAddon(MinecraftClient.getInstance().getNetworkHandler());
		}

		// We haven't hit the end of onGameJoin yet, use our backing field here to access the network handler
		if (currentPlayAddon != null) {
			return currentPlayAddon;
		}

		// We are not in play stage
		return null;
	}

	public static void setClientPlayAddon(ClientPlayNetworkAddon addon) {
		currentPlayAddon = addon;
	}

	public static void setClientConfigurationAddon(ClientConfigurationNetworkAddon addon) {
		currentConfigurationAddon = addon;
	}

	public static void clientInit(ModContainer mod) {
		// Reference cleanup for the locally stored addon if we are disconnected
		ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
			currentPlayAddon = null;
		});

		ClientConfigurationConnectionEvents.DISCONNECT.register((handler, client) -> {
			currentConfigurationAddon = null;
		});

		PayloadTypeRegistry.configurationS2C().register(CommonVersionPayload.PACKET_ID, CommonVersionPayload.CODEC);
		PayloadTypeRegistry.configurationS2C().register(CommonRegisterPayload.PACKET_ID, CommonRegisterPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(CommonVersionPayload.PACKET_ID, CommonVersionPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(CommonRegisterPayload.PACKET_ID, CommonRegisterPayload.CODEC);

		ClientConfigurationNetworking.registerGlobalReceiver(CommonVersionPayload.PACKET_ID, ClientNetworkingImpl::handleCommonVersion);
		ClientConfigurationNetworking.registerGlobalReceiver(CommonRegisterPayload.PACKET_ID, ClientNetworkingImpl::handleCommonRegister);
	}

	private static void handleCommonVersion(MinecraftClient client, ClientConfigurationNetworkHandler handler, CommonVersionPayload payload, PacketSender<CustomPayload> sender) {
		int negotiatedVersion = handleVersionPacket(payload, sender);
		ClientNetworkingImpl.getAddon(handler).onCommonVersionPacket(negotiatedVersion);
	}

	private static void handleCommonRegister(MinecraftClient client, ClientConfigurationNetworkHandler handler, CommonRegisterPayload payload, PacketSender<CustomPayload> sender) {
		ClientConfigurationNetworkAddon addon = ClientNetworkingImpl.getAddon(handler);

		if (CommonRegisterPayload.PLAY_PHASE.equals(payload.phase())) {
			if (payload.version() != addon.getNegotiatedVersion()) {
				throw new IllegalStateException("Negotiated common packet version: %d but received packet with version: %d".formatted(addon.getNegotiatedVersion(), payload.version()));
			}

			addon.getChannelInfoHolder().getPendingChannelsNames(NetworkPhase.PLAY).addAll(payload.channels());
			NetworkingImpl.LOGGER.debug("Received accepted channels from the server");
			sender.sendPayload(new CommonRegisterPayload(addon.getNegotiatedVersion(), CommonRegisterPayload.PLAY_PHASE, ClientPlayNetworking.getGlobalReceivers()));
		} else {
			addon.onCommonRegisterPacket(payload);
			sender.sendPayload(addon.createRegisterPayload());
		}
	}

	// Disconnect if there are no commonly supported versions.
	// Client responds with the intersection of supported versions.
	// Return the highest supported version
	private static int handleVersionPacket(CommonVersionPayload payload, PacketSender<CustomPayload> packetSender) {
		int version = CommonPacketsImpl.getHighestCommonVersion(payload.versions(), CommonPacketsImpl.SUPPORTED_COMMON_PACKET_VERSIONS);

		if (version <= 0) {
			throw new UnsupportedOperationException("Client does not support any requested versions from server");
		}

		packetSender.sendPayload(new CommonVersionPayload(new int[]{version}));
		return version;
	}
}

