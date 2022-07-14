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
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import org.quiltmc.qsl.registry.impl.sync.DelayedPacketsHolder;
import org.quiltmc.qsl.registry.impl.sync.ServerRegistrySync;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
	@Inject(method = "onPlayerConnect", at = @At("TAIL"))
	private void quilt$sendSync(ClientConnection connection, ServerPlayerEntity player, CallbackInfo ci) {
		var delayedList = ((DelayedPacketsHolder) player).quilt$getPacketList();

		if (delayedList != null) {
			for (var packet : delayedList) {
				packet.apply(player.networkHandler);
			}
		}

		((DelayedPacketsHolder) player).quilt$setPacketList(null);
	}
}
