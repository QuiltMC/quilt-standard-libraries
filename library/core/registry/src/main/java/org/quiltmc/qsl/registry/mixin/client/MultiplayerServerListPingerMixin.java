/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.registry.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArgs;
import org.spongepowered.asm.mixin.injection.invoke.arg.Args;

import net.minecraft.client.network.MultiplayerServerListPinger;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.network.listener.ClientQueryPacketListener;
import net.minecraft.network.packet.s2c.query.QueryPongS2CPacket;
import net.minecraft.network.packet.s2c.query.ServerMetadataS2CPacket;
import net.minecraft.text.Text;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.registry.impl.sync.mod_protocol.ModProtocolContainer;

@ClientOnly
@Mixin(MultiplayerServerListPinger.class)
public class MultiplayerServerListPingerMixin {
	@ModifyArgs(method = "add", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/ClientConnection;connect(Ljava/lang/String;ILnet/minecraft/network/listener/ClientQueryPacketListener;)V"))
	private void quilt$attachModProtocol(Args args, ServerInfo entry, Runnable pinger) {
		var queryPacketListener = (ClientQueryPacketListener) args.get(0);
		args.set(0, new ClientQueryPacketListener() {
			@Override
			public void onServerMetadata(ServerMetadataS2CPacket packet) {
				if (packet.status().version().isPresent()) {
					var x = ModProtocolContainer.of(packet.status().version().get()).quilt$getModProtocol();
					ModProtocolContainer.of(entry).quilt$setModProtocol(x);
				}

				queryPacketListener.onServerMetadata(packet);
			}

			@Override
			public void onQueryPong(QueryPongS2CPacket packet) {
				queryPacketListener.onQueryPong(packet);
			}

			@Override
			public void onDisconnected(Text reason) {
				queryPacketListener.onDisconnected(reason);
			}

			@Override
			public boolean isConnected() {
				return queryPacketListener.isConnected();
			}
		});
	}
}
