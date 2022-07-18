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

package org.quiltmc.qsl.component.impl.sync.packet;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerLoginNetworkHandler;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.networking.api.PacketByteBufs;

public final class RegistryPacket {
	public static <T> PacketByteBuf createRegistryPacket(Registry<T> registry) {
		var buf = PacketByteBufs.create();
		buf.writeInt(registry.size()); // append size

		registry.forEach(t -> {
			var id = registry.getId(t);
			var rawId = registry.getRawId(t);

			buf.writeIdentifier(id).writeVarInt(rawId); // append id and rawId
		});

		return buf;
	}

	public static void handleRegistryResponse(PacketByteBuf buf, ServerLoginNetworkHandler handler, String msg) {
		String retString = buf.readString();

		if (!retString.equals("Ok")) { // a handled packet should return 'Ok'
			Identifier id = Identifier.tryParse(retString); // a failed one should return the id that caused a desync

			// In case we failed, we disconnect the client since we cannot validly sync components with it
			handler.disconnect(Text.literal(msg.formatted(id)));
			ComponentsImpl.LOGGER.warn(msg.formatted(id));
		}
	}
}
