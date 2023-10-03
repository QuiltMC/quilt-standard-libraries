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
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientNetworkHandler;
import net.minecraft.client.network.ClientConfigurationNetworkHandler;
import net.minecraft.network.ClientConnection;
import net.minecraft.unmapped.C_qqflkeyp;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.impl.NetworkHandlerExtensions;
import org.quiltmc.qsl.networking.impl.client.ClientConfigurationNetworkAddon;
import org.quiltmc.qsl.networking.impl.client.ClientNetworkingImpl;

// We want to apply a bit earlier than other mods which may not use us in order to prevent refCount issues
@ClientOnly
@Mixin(value = ClientConfigurationNetworkHandler.class, priority = 999)
abstract class ClientConfigurationNetworkHandlerMixin extends AbstractClientNetworkHandler implements NetworkHandlerExtensions {
	@Unique
	private ClientConfigurationNetworkAddon addon;

	protected ClientConfigurationNetworkHandlerMixin(MinecraftClient client, ClientConnection connection, C_qqflkeyp c_qqflkeyp) {
		super(client, connection, c_qqflkeyp);
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void initAddon(CallbackInfo ci) {
		this.addon = new ClientConfigurationNetworkAddon((ClientConfigurationNetworkHandler) (Object) this, this.client);
		// A bit of a hack but it allows the field above to be set in case someone registers handlers during INIT event which refers to said field
		ClientNetworkingImpl.setClientConfigurationAddon(this.addon);
		this.addon.lateInit();
	}

	@Override
	public ClientConfigurationNetworkAddon getAddon() {
		return this.addon;
	}
}
