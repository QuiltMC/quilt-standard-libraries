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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.AbstractClientNetworkHandler;
import net.minecraft.network.DisconnectionDetails;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.impl.client.ClientConfigurationNetworkAddon;
import org.quiltmc.qsl.networking.impl.client.ClientPlayNetworkAddon;
import org.quiltmc.qsl.networking.impl.NetworkHandlerExtensions;

// We want to apply a bit earlier than other mods which may not use us in order to prevent refCount issues
@ClientOnly
@Mixin(value = AbstractClientNetworkHandler.class, priority = 999)
abstract class AbstractClientNetworkHandlerMixin implements NetworkHandlerExtensions {
	@Inject(method = "onCustomPayload(Lnet/minecraft/network/packet/s2c/common/CustomPayloadS2CPacket;)V", at = @At("HEAD"), cancellable = true)
	public void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo ci) {
		final CustomPayload payload = packet.payload();
		boolean handled;

		if (this.getAddon() instanceof ClientPlayNetworkAddon addon) {
			handled = addon.handle(payload);
		} else if (this.getAddon() instanceof ClientConfigurationNetworkAddon addon) {
			handled = addon.handle(payload);
		} else {
			throw new IllegalStateException("Unknown network addon");
		}

		if (handled) {
			ci.cancel();
		}
	}

	@Inject(method = "onDisconnected", at = @At("HEAD"))
	private void handleDisconnection(DisconnectionDetails details, CallbackInfo ci) {
		this.getAddon().handleDisconnect();
	}
}
