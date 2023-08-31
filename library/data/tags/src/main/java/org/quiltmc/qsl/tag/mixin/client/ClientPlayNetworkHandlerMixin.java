/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.tag.mixin.client;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.registry.ClientRegistryLayer;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.GameJoinS2CPacket;
import net.minecraft.registry.LayeredRegistryManager;
import net.minecraft.text.Text;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.tag.impl.TagRegistryImpl;
import org.quiltmc.qsl.tag.impl.client.ClientRegistryStatus;
import org.quiltmc.qsl.tag.impl.client.ClientTagRegistryManager;

@ClientOnly
@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
	@Shadow
	@Final
	private ClientConnection connection;

	@Shadow
	private LayeredRegistryManager<ClientRegistryLayer> clientRegistryManager;

	@Inject(
			method = "onGameJoin",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/ClientConnection;isLocal()Z",
					shift = At.Shift.BEFORE
			)
	)
	private void onGameJoin(GameJoinS2CPacket packet, CallbackInfo ci) {
		ClientTagRegistryManager.applyAll(this.clientRegistryManager.getManager(ClientRegistryLayer.REMOTE), ClientRegistryStatus.REMOTE);
	}

	@Inject(method = "onDisconnected", at = @At("TAIL"))
	private void onDisconnected(Text reason, CallbackInfo ci) {
		ClientTagRegistryManager.resetDynamicAll(true);

		if (!this.connection.isLocal()) {
			TagRegistryImpl.resetTags();
		}
	}
}
