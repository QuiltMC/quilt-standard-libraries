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

package org.quiltmc.qsl.networking.mixin;

import org.quiltmc.qsl.networking.impl.AbstractNetworkAddon;
import org.quiltmc.qsl.networking.impl.NetworkHandlerExtensions;
import org.quiltmc.qsl.networking.impl.payload.PacketByteBufPayload;
import org.quiltmc.qsl.networking.impl.server.ServerConfigurationNetworkAddon;
import org.quiltmc.qsl.networking.impl.server.ServerPlayNetworkAddon;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.AbstractServerPacketHandler;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.c2s.common.PongC2SPacket;
import net.minecraft.server.MinecraftServer;

// We want to apply a bit earlier than other mods which may not use us in order to prevent refCount issues
@Mixin(value = AbstractServerPacketHandler.class, priority = 999)
abstract class AbstractServerPacketHandlerMixin implements NetworkHandlerExtensions {
	@Shadow
	@Final
	protected MinecraftServer server;
	@Shadow
	@Final
	protected ClientConnection connection;

	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	private void handleCustomPayloadReceivedAsync(CustomPayloadC2SPacket packet, CallbackInfo ci) {
		if (packet.payload() instanceof PacketByteBufPayload payload) {
			boolean payloadHandled;
			AbstractNetworkAddon<?> addon = this.getAddon();
			if (addon instanceof ServerPlayNetworkAddon play) {
				payloadHandled = play.handle(payload);
			} else if (addon instanceof ServerConfigurationNetworkAddon configuration) {
				payloadHandled = configuration.handle(payload);
			} else {
				throw new IllegalStateException("Unknown Server Addon");
			}

			if (payloadHandled) {
				ci.cancel();
			} else {
				payload.data().skipBytes(payload.data().readableBytes());
			}
		}
	}

	@Inject(method = "onPlayPong", at = @At("HEAD"))
	private void onPlayPong(PongC2SPacket packet, CallbackInfo ci) {
		if (getAddon() instanceof ServerConfigurationNetworkAddon addon) {
			addon.onPong(packet.getParameter());
		}
	}
}
