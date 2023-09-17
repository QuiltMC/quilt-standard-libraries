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

package org.quiltmc.qsl.networking.impl.client;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ConnectScreen;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkState;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.listener.ServerCommonPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.client.ClientConfigurationConnectionEvents;
import org.quiltmc.qsl.networking.api.client.ClientConfigurationNetworking;
import org.quiltmc.qsl.networking.api.client.ClientLoginNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.networking.impl.ChannelInfoHolder;
import org.quiltmc.qsl.networking.impl.GlobalReceiverRegistry;
import org.quiltmc.qsl.networking.impl.NetworkHandlerExtensions;
import org.quiltmc.qsl.networking.impl.NetworkingImpl;
import org.quiltmc.qsl.networking.impl.payload.PacketByteBufPayload;
import org.quiltmc.qsl.networking.mixin.accessor.ClientLoginNetworkHandlerAccessor;
import org.quiltmc.qsl.networking.mixin.accessor.ConnectScreenAccessor;
import org.quiltmc.qsl.networking.mixin.accessor.MinecraftClientAccessor;

@ApiStatus.Internal
@ClientOnly
public final class ClientNetworkingImpl {
	public static final GlobalReceiverRegistry<ClientLoginNetworking.QueryRequestReceiver> LOGIN = new GlobalReceiverRegistry<>(NetworkState.LOGIN);
	public static final GlobalReceiverRegistry<ClientConfigurationNetworking.ChannelReceiver> CONFIGURATION = new GlobalReceiverRegistry<>(NetworkState.CONFIGURATION);
	public static final GlobalReceiverRegistry<ClientPlayNetworking.ChannelReceiver> PLAY = new GlobalReceiverRegistry<>(NetworkState.PLAY);
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

	public static Packet<ServerCommonPacketListener> createC2SPacket(Identifier channelName, PacketByteBuf buf) {
		return new CustomPayloadC2SPacket(new PacketByteBufPayload(channelName, buf));
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

		// Register a login query handler for early channel registration.
		ClientLoginNetworking.registerGlobalReceiver(NetworkingImpl.EARLY_REGISTRATION_CHANNEL, ClientNetworkingImpl::receiveEarlyRegistration);
		ClientLoginNetworking.registerGlobalReceiver(NetworkingImpl.EARLY_REGISTRATION_CHANNEL_FABRIC, ClientNetworkingImpl::receiveEarlyRegistration);
	}

	private static CompletableFuture<PacketByteBuf> receiveEarlyRegistration(
			MinecraftClient client, ClientLoginNetworkHandler handler, PacketByteBuf buf, Consumer<PacketSendListener> listenerAdder
	) {
		int n = buf.readVarInt();
		var ids = new ArrayList<Identifier>(n);

		for (int i = 0; i < n; i++) {
			ids.add(buf.readIdentifier());
		}

		((ChannelInfoHolder) ((ClientLoginNetworkHandlerAccessor) handler).getConnection()).getPendingChannelsNames(NetworkState.LOGIN).addAll(ids);
		NetworkingImpl.LOGGER.debug("Received accepted channels from the server");

		PacketByteBuf response = PacketByteBufs.create();
		Collection<Identifier> channels = ClientPlayNetworking.getGlobalReceivers();
		response.writeVarInt(channels.size());

		for (Identifier id : channels) {
			response.writeIdentifier(id);
		}

		NetworkingImpl.LOGGER.debug("Sent accepted channels to the server");
		return CompletableFuture.completedFuture(response);
	}
}

