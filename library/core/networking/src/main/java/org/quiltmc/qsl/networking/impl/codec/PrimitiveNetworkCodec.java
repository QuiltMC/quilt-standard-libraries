package org.quiltmc.qsl.networking.impl.codec;

import net.minecraft.network.PacketByteBuf;

import org.quiltmc.qsl.networking.api.codec.NetworkCodec;

public final class PrimitiveNetworkCodec {
	public static final class Boolean implements NetworkCodec<java.lang.Boolean> {
		@Override
		public java.lang.Boolean decode(PacketByteBuf buf) {
			return this.decodeBoolean(buf);
		}

		@Override
		public void encode(PacketByteBuf buf, java.lang.Boolean data) {
			this.encodeBoolean(buf, data);
		}

		public boolean decodeBoolean(PacketByteBuf buf) {
			return buf.readBoolean();
		}

		public void encodeBoolean(PacketByteBuf buf, boolean data) {
			buf.writeBoolean(data);
		}

		@Override
		public String toString() {
			return "Boolean";
		}
	}

	public static final class Byte implements NetworkCodec<java.lang.Byte> {
		@Override
		public java.lang.Byte decode(PacketByteBuf buf) {
			return this.decodeByte(buf);
		}

		@Override
		public void encode(PacketByteBuf buf, java.lang.Byte data) {
			this.encodeByte(buf, data);
		}

		public byte decodeByte(PacketByteBuf buf) {
			return buf.readByte();
		}

		public void encodeByte(PacketByteBuf buf, byte data) {
			buf.writeByte(data);
		}

		@Override
		public String toString() {
			return "Byte";
		}
	}

	public static final class Char implements NetworkCodec<Character> {
		@Override
		public Character decode(PacketByteBuf buf) {
			return this.decodeChar(buf);
		}

		@Override
		public void encode(PacketByteBuf buf, Character data) {
			this.encodeChar(buf, data);
		}

		public char decodeChar(PacketByteBuf buf) {
			return buf.readChar();
		}

		public void encodeChar(PacketByteBuf buf, char data) {
			buf.writeChar(data);
		}

		@Override
		public String toString() {
			return "Char";
		}
	}

	public static final class Short implements NetworkCodec<java.lang.Short> {
		@Override
		public java.lang.Short decode(PacketByteBuf buf) {
			return this.decodeShort(buf);
		}

		@Override
		public void encode(PacketByteBuf buf, java.lang.Short data) {
			this.encodeShort(buf, data);
		}

		public short decodeShort(PacketByteBuf buf) {
			return buf.readShort();
		}

		public void encodeShort(PacketByteBuf buf, short data) {
			buf.writeShort(data);
		}

		@Override
		public String toString() {
			return "Short";
		}
	}

	public static class Int implements NetworkCodec<Integer> {
		@Override
		public Integer decode(PacketByteBuf buf) {
			return this.decodeInt(buf);
		}

		@Override
		public void encode(PacketByteBuf buf, Integer data) {
			this.encodeInt(buf, data);
		}

		public int decodeInt(PacketByteBuf buf) {
			return buf.readInt();
		}

		public void encodeInt(PacketByteBuf buf, int data) {
			buf.writeInt(data);
		}

		@Override
		public String toString() {
			return "Int";
		}
	}

	public static final class VarInt extends Int {
		@Override
		public int decodeInt(PacketByteBuf buf) {
			return buf.readVarInt();
		}

		@Override
		public void encodeInt(PacketByteBuf buf, int data) {
			buf.writeVarInt(data);
		}

		@Override
		public String toString() {
			return "VarInt";
		}
	}

	public static class Long implements NetworkCodec<java.lang.Long> {
		@Override
		public java.lang.Long decode(PacketByteBuf buf) {
			return this.decodeLong(buf);
		}

		@Override
		public void encode(PacketByteBuf buf, java.lang.Long data) {
			this.encodeLong(buf, data);
		}

		public long decodeLong(PacketByteBuf buf) {
			return buf.readLong();
		}

		public void encodeLong(PacketByteBuf buf, long data) {
			buf.writeLong(data);
		}

		@Override
		public String toString() {
			return "Long";
		}
	}

	public static final class VarLong extends Long {
		@Override
		public long decodeLong(PacketByteBuf buf) {
			return buf.readVarLong();
		}

		@Override
		public void encodeLong(PacketByteBuf buf, long data) {
			buf.writeVarLong(data);
		}

		@Override
		public String toString() {
			return "VarLong";
		}
	}
}
