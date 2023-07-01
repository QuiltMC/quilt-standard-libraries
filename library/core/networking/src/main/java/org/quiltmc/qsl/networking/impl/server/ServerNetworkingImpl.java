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

package org.quiltmc.qsl.networking.impl.server;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.ServerLoginNetworking;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.impl.GlobalReceiverRegistry;
import org.quiltmc.qsl.networking.impl.NetworkHandlerExtensions;

@ApiStatus.Internal
public final class ServerNetworkingImpl {
	public static final GlobalReceiverRegistry<ServerLoginNetworking.QueryResponseReceiver> LOGIN = new GlobalReceiverRegistry<>();
	public static final GlobalReceiverRegistry<ServerPlayNetworking.ChannelReceiver> PLAY = new GlobalReceiverRegistry<>();

	public static ServerPlayNetworkAddon getAddon(ServerPlayNetworkHandler handler) {
		return (ServerPlayNetworkAddon) ((NetworkHandlerExtensions) handler).getAddon();
	}

	public static ServerLoginNetworkAddon getAddon(ServerLoginNetworkHandler handler) {
		return (ServerLoginNetworkAddon) ((NetworkHandlerExtensions) handler).getAddon();
	}

	public static Packet<ClientPlayPacketListener> createPlayC2SPacket(Identifier channel, PacketByteBuf buf) {
		return new CustomPayloadS2CPacket(channel, buf);
	}
}
