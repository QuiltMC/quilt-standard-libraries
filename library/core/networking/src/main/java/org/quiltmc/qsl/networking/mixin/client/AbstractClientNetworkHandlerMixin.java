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

package org.quiltmc.qsl.networking.mixin.client;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.impl.NetworkHandlerExtensions;
import org.quiltmc.qsl.networking.impl.client.ClientConfigurationNetworkAddon;
import org.quiltmc.qsl.networking.impl.client.ClientPlayNetworkAddon;
import org.quiltmc.qsl.networking.impl.payload.PacketByteBufPayload;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.AbstractClientNetworkHandler;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.text.Text;

// We want to apply a bit earlier than other mods which may not use us in order to prevent refCount issues
@ClientOnly
@Mixin(value = AbstractClientNetworkHandler.class, priority = 999)
abstract class AbstractClientNetworkHandlerMixin implements NetworkHandlerExtensions {
	@Inject(method = "onCustomPayload", at = @At("HEAD"), cancellable = true)
	private void handleCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		if (packet.payload() instanceof PacketByteBufPayload payload) {
			boolean handledPayload;

			if (this.getAddon() instanceof ClientPlayNetworkAddon addon) {
				handledPayload = addon.handle(payload);
			} else if (this.getAddon() instanceof ClientConfigurationNetworkAddon addon) {
				handledPayload = addon.handle(payload);
			} else {
				throw new IllegalStateException("Unknown Client Network addon");
			}

			if (handledPayload) {
				ci.cancel();
			} else {
				payload.data().skipBytes(payload.data().readableBytes());
			}
		}
	}

	@Inject(method = "onDisconnected", at = @At("HEAD"))
	private void handleDisconnection(Text reason, CallbackInfo ci) {
		this.getAddon().handleDisconnect();
	}
}
