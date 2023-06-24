/*
 * Copyright 2016, 2017, 2018, 2019, 2020 zml and Colonel contributors
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

package org.quiltmc.qsl.command.impl;

import java.util.HashSet;
import java.util.Set;

import io.netty.buffer.Unpooled;
import org.jetbrains.annotations.ApiStatus;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerLoginConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerLoginNetworking;
import org.quiltmc.qsl.networking.api.client.ClientLoginNetworking;

@ApiStatus.Internal
public final class KnownArgTypesSync {
	private KnownArgTypesSync() {
	}

	public static final Identifier ID = Initializer.id("known_arg_types");

	public static void register() {
		ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) ->
				sender.sendPacket(ID, PacketByteBufs.empty()));
		ServerLoginNetworking.registerGlobalReceiver(ID, (server, handler, understood, buf, synchronizer, responseSender) -> {
			if (understood) {
				var idents = buf.readCollection(HashSet::new, PacketByteBuf::readIdentifier);
				synchronizer.waitFor(server.submit(() -> ServerArgumentTypes.setKnownArgumentTypes(handler, idents)));
			} else {
				synchronizer.waitFor(server.submit(() -> ServerArgumentTypes.setKnownArgumentTypes(handler, Set.of())));
			}
		});
	}

	@ClientOnly
	public static void registerClient() {
		ClientLoginNetworking.registerGlobalReceiver(ID, (client, handler, buf, listenerAdder) -> client.submit(() -> {
			var idents = ServerArgumentTypes.getIds();
			var responseBuf = new PacketByteBuf(Unpooled.buffer(idents.size() * 8));
			responseBuf.writeCollection(idents, PacketByteBuf::writeIdentifier);
			return responseBuf;
		}));
	}
}
