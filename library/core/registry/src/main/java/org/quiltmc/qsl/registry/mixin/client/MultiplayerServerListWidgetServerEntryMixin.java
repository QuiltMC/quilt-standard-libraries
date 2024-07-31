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

import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.client.gui.widget.list.multiplayer.ServerEntryListWidget;
import net.minecraft.client.network.ServerInfo;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.registry.impl.sync.ProtocolVersions;
import org.quiltmc.qsl.registry.impl.sync.mod_protocol.ModProtocolContainer;
import org.quiltmc.qsl.registry.impl.sync.mod_protocol.ModProtocolImpl;

@ClientOnly
@Mixin(ServerEntryListWidget.ServerEntry.class)
public class MultiplayerServerListWidgetServerEntryMixin {
	@Shadow
	@Final
	private ServerInfo server;

	@Dynamic("method_55816: second lambda in getServerListPinger().add(...) in render(...)")
	@ModifyArg(method = "method_55816", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ServerInfo;setPingResult(Lnet/minecraft/client/network/ServerInfo$PingResult;)V"))
	private ServerInfo.PingResult quilt$checkModProtocol(ServerInfo.PingResult result) {
		var map = ModProtocolContainer.of(this.server).quilt$getModProtocol();

		if (map != null) {
			for (var entry : map.entrySet()) {
				var c = ModProtocolImpl.getVersion(entry.getKey());
				if (ProtocolVersions.getHighestSupported(c, entry.getValue()) == ProtocolVersions.NO_PROTOCOL) {
					return ServerInfo.PingResult.INCOMPATIBLE;
				}
			}
		}
		return result;
	}
}
