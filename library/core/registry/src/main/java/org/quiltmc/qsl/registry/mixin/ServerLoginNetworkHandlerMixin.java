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

package org.quiltmc.qsl.registry.mixin;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.listener.PacketListener;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import org.quiltmc.qsl.registry.impl.sync.ServerRegistrySync;
import org.quiltmc.qsl.registry.impl.sync.ServerRegistrySyncNetworkHandler;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerLoginNetworkHandler.class)
public abstract class ServerLoginNetworkHandlerMixin {
	@Shadow
	@Final
	public ClientConnection connection;

	@Shadow
	protected abstract void addToServer(ServerPlayerEntity player);

	@Unique
	private boolean quilt$continueJoining = false;

	@Inject(method = "addToServer", at = @At("HEAD"), cancellable = true)
	private void quilt$applySyncHandler(ServerPlayerEntity player, CallbackInfo ci) {
		if (!player.server.isHost(player.getGameProfile()) && !this.quilt$continueJoining && ServerRegistrySync.shouldSync()) {
			this.connection.setPacketListener(new ServerRegistrySyncNetworkHandler(player, this.connection, () -> {
				this.quilt$continueJoining = true;
				this.connection.setPacketListener((PacketListener) this);
				this.addToServer(player);
			}));
			ci.cancel();
		}
	}
}
