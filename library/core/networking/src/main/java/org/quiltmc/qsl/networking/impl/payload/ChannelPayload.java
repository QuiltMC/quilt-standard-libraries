package org.quiltmc.qsl.networking.impl.payload;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import io.netty.util.AsciiString;
import org.quiltmc.qsl.networking.impl.NetworkingImpl;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;
import net.minecraft.util.InvalidIdentifierException;

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
//			LOGGER.warn("Received invalid channel identifier \"{}\"", literal);
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
