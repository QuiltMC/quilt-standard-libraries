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

package org.quiltmc.qsl.networking.mixin;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;

import io.netty.channel.ChannelHandlerContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkSide;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.impl.ChannelInfoHolder;
import org.quiltmc.qsl.networking.impl.DisconnectPacketSource;
import org.quiltmc.qsl.networking.impl.NetworkHandlerExtensions;
import org.quiltmc.qsl.networking.impl.PacketCallbackListener;

@Mixin(ClientConnection.class)
abstract class ClientConnectionMixin implements ChannelInfoHolder {
	@Shadow
	private PacketListener packetListener;

	@Shadow
	public abstract void send(Packet<?> packet, PacketSendListener listener);

	@Shadow
	public abstract void disconnect(Text disconnectReason);

	@Unique
	private Collection<Identifier> playChannels;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void initAddedFields(NetworkSide side, CallbackInfo ci) {
		this.playChannels = Collections.newSetFromMap(new ConcurrentHashMap<>());
	}

	// Must be fully qualified due to mixin not working in production without it
	@Redirect(
			method = "exceptionCaught",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketSendListener;)V"
			)
	)
	private void resendOnExceptionCaught(ClientConnection self, Packet<?> packet, PacketSendListener listener,
			ChannelHandlerContext channelHandlerContext, Throwable throwable) {
		if (this.packetListener instanceof DisconnectPacketSource dcSource) {
			this.send(
					dcSource.createDisconnectPacket(
							Text.translatable("disconnect.genericReason", "Internal Exception: " + throwable)
					),
					listener
			);
		} else {
			// Don't send packet if we cannot send proper packets
			this.disconnect(Text.translatable("disconnect.genericReason", "Internal Exception: " + throwable));
		}
	}

	@Inject(method = "sendImmediately", at = @At(value = "FIELD", target = "Lnet/minecraft/network/ClientConnection;packetsSentCounter:I"))
	private void checkPacket(Packet<?> packet, PacketSendListener listener, CallbackInfo ci) {
		if (this.packetListener instanceof PacketCallbackListener callbackListener) {
			callbackListener.sent(packet);
		}
	}

	@Inject(method = "channelInactive", at = @At("HEAD"))
	private void handleDisconnect(ChannelHandlerContext channelHandlerContext, CallbackInfo ci) throws Exception {
		if (this.packetListener instanceof NetworkHandlerExtensions ext) { // not the case for client/server query
			ext.getAddon().handleDisconnect();
		}
	}

	@Override
	public Collection<Identifier> getPendingChannelsNames() {
		return this.playChannels;
	}
}
