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

package org.quiltmc.qsl.networking.impl.payload;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import io.netty.util.AsciiString;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

import org.quiltmc.qsl.networking.impl.NetworkingImpl;

public interface ChannelPayload extends CustomPayload {
	private static void write(List<Identifier> channels, PacketByteBuf buf) {
		boolean first = true;

		for (Identifier channel : channels) {
			if (first) {
				first = false;
			} else {
				buf.writeByte(0);
			}

			buf.writeBytes(channel.toString().getBytes(StandardCharsets.US_ASCII));
		}
	}

	private static List<Identifier> readIds(PacketByteBuf buf) {
		List<Identifier> ids = new ArrayList<>();
		StringBuilder active = new StringBuilder();

		while (buf.isReadable()) {
			byte b = buf.readByte();

			if (b != 0) {
				active.append(AsciiString.b2c(b));
			} else {
				addId(ids, active);
				active = new StringBuilder();
			}
		}

		addId(ids, active);

		return ids;
	}

	private static void addId(List<Identifier> ids, StringBuilder sb) {
		String literal = sb.toString();

		try {
			ids.add(new Identifier(literal));
		} catch (InvalidIdentifierException ex) {
			NetworkingImpl.LOGGER.warn("Received invalid channel identifier \"{}\"", literal);
		}
	}

	List<Identifier> channels();

	record RegisterChannelPayload(List<Identifier> channels) implements ChannelPayload {
		public RegisterChannelPayload(PacketByteBuf buf) {
			this(ChannelPayload.readIds(buf));
		}

		@Override
		public void write(PacketByteBuf buf) {
			ChannelPayload.write(this.channels, buf);
		}

		@Override
		public Identifier id() {
			return NetworkingImpl.REGISTER_CHANNEL;
		}
	}

	record UnregisterChannelPayload(List<Identifier> channels) implements ChannelPayload {
		public UnregisterChannelPayload(PacketByteBuf buf) {
			this(ChannelPayload.readIds(buf));
		}

		@Override
		public void write(PacketByteBuf buf) {
			ChannelPayload.write(this.channels, buf);
		}

		@Override
		public Identifier id() {
			return NetworkingImpl.UNREGISTER_CHANNEL;
		}
	}
}
