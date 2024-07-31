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

package org.quiltmc.qsl.networking.impl;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.networking.api.PayloadTypeRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.network.NetworkPhase;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.network.packet.s2c.login.payload.CustomQueryPayload;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerLoginNetworkHandler;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.networking.api.PacketSender;
import org.quiltmc.qsl.networking.api.server.ServerLoginNetworking;
import org.quiltmc.qsl.networking.impl.payload.ChannelPayload;
import org.quiltmc.qsl.networking.mixin.accessor.ServerLoginNetworkHandlerAccessor;

@ApiStatus.Internal
public final class NetworkingImpl {
	public static final String MOD_ID = "quilt_networking";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
	/**
	 * Identifier of packet used to register supported channels.
	 */
	public static final CustomPayload.Id<ChannelPayload.RegisterChannelPayload> REGISTER_CHANNEL = CustomPayload.create("register");
	/**
	 * Identifier of packet used to unregister supported channels.
	 */
	public static final CustomPayload.Id<ChannelPayload.UnregisterChannelPayload> UNREGISTER_CHANNEL = CustomPayload.create("unregister");

	public static void init(ModContainer mod) {
		PayloadTypeRegistry.configurationC2S().register(REGISTER_CHANNEL, ChannelPayload.RegisterChannelPayload.CODEC);
		PayloadTypeRegistry.configurationC2S().register(UNREGISTER_CHANNEL, ChannelPayload.UnregisterChannelPayload.CODEC);
		PayloadTypeRegistry.configurationS2C().register(REGISTER_CHANNEL, ChannelPayload.RegisterChannelPayload.CODEC);
		PayloadTypeRegistry.configurationS2C().register(UNREGISTER_CHANNEL, ChannelPayload.UnregisterChannelPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(REGISTER_CHANNEL, ChannelPayload.RegisterChannelPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(UNREGISTER_CHANNEL, ChannelPayload.UnregisterChannelPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(REGISTER_CHANNEL, ChannelPayload.RegisterChannelPayload.CODEC);
		PayloadTypeRegistry.playC2S().register(UNREGISTER_CHANNEL, ChannelPayload.UnregisterChannelPayload.CODEC);
	}

	public static boolean isReservedCommonChannel(CustomPayload.Id<?> channelName) {
		return channelName.equals(REGISTER_CHANNEL) || channelName.equals(UNREGISTER_CHANNEL);
	}

	private static void receiveEarlyRegistration(MinecraftServer server, ServerLoginNetworkHandler handler, boolean understood, PacketByteBuf buf, ServerLoginNetworking.LoginSynchronizer synchronizer, PacketSender<CustomQueryPayload> sender) {
		if (!understood) {
			// The client is likely a vanilla client.
			return;
		}

		int n = buf.readVarInt();
		List<CustomPayload.Id<?>> ids = new ArrayList<>(n);

		for (int i = 0; i < n; i++) {
			ids.add(new CustomPayload.Id<>(buf.readIdentifier()));
		}

		((ChannelInfoHolder) ((ServerLoginNetworkHandlerAccessor) handler).getConnection()).getPendingChannelsNames(NetworkPhase.LOGIN).addAll(ids);
		NetworkingImpl.LOGGER.debug("Received accepted channels from the client for \"{}\"", handler.getConnectionInfo());
	}
}
