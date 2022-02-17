/*
 * Copyright 2016, 2017, 2018, 2019, 2020 zml and Colonel contributors
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

package org.quiltmc.qsl.command.impl;

import java.util.HashSet;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.C2SPlayChannelEvents;

@ApiStatus.Internal
public final class KnownArgumentTypesSync {
	private KnownArgumentTypesSync() {
	}

	public static final Identifier ID = Initializer.id("registered_arg_types");

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(ID, (server, player, handler, buf, responseSender) -> {
			var idents = buf.readCollection(HashSet::new, PacketByteBuf::readIdentifier);
			server.execute(() -> {
				ServerArgumentTypes.setKnownArgumentTypes(player, idents);
			});
		});
	}

	@Environment(EnvType.CLIENT)
	public static void registerClient() {
		C2SPlayChannelEvents.REGISTER.register((handler, sender, client, channels) -> {
			client.execute(() -> {
				var idents = ServerArgumentTypes.getIds();
				var buf = new PacketByteBuf(Unpooled.buffer(idents.size() * 8));
				buf.writeCollection(idents, PacketByteBuf::writeIdentifier);
				sender.sendPacket(ID, buf);
			});
		});
	}
}
