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

package org.quiltmc.qsl.networking.api.codec;

import java.util.function.DoubleFunction;
import java.util.function.IntFunction;
import java.util.function.LongFunction;
import java.util.function.Predicate;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;

import net.minecraft.network.PacketByteBuf;

public final class PrimitiveNetworkCodec {
	public enum Null implements NetworkCodec<Void> {
		INSTANCE;

		@Override
		public Void decode(PacketByteBuf buf) {
			return null;
		}

		@Override
		public void encode(PacketByteBuf buf, Void data) { }
	}

	public enum Boolean implements NetworkCodec<java.lang.Boolean> {
		INSTANCE;

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

		public <A> NetworkCodec<A> mapBoolean(Predicate<A> to, FromBoolean<A> from) {
			return new SimpleNetworkCodec<>(
					(byteBuf, a) -> this.encodeBoolean(byteBuf, to.test(a)),
					byteBuf -> from.fromBoolean(this.decodeBoolean(byteBuf))
			);
		}

		@Override
		public String toString() {
			return "Boolean";
		}

		public interface FromBoolean<A> {
			A fromBoolean(boolean b);
		}
	}

	public enum Byte implements NetworkCodec<java.lang.Byte> {
		INSTANCE;

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

		public <A> NetworkCodec<A> mapByte(ToByte<A> to, FromByte<A> from) {
			return new SimpleNetworkCodec<>(
					(byteBuf, a) -> this.encodeByte(byteBuf, to.toByte(a)),
					byteBuf -> from.fromByte(this.decodeByte(byteBuf))
			);
		}

		@Override
		public String toString() {
			return "Byte";
		}

		public interface ToByte<A> {
			byte toByte(A a);
		}

		public interface FromByte<A> {
			A fromByte(byte b);
		}
	}

	public enum Char implements NetworkCodec<Character> {
		INSTANCE;

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

		public <A> NetworkCodec<A> mapChar(ToChar<A> to, FromChar<A> from) {
			return new SimpleNetworkCodec<>(
					(byteBuf, a) -> this.encodeChar(byteBuf, to.toChar(a)),
					byteBuf -> from.fromChar(this.decodeChar(byteBuf))
			);
		}

		@Override
		public String toString() {
			return "Char";
		}

		public interface ToChar<A> {
			char toChar(A a);
		}

		public interface FromChar<A> {
			A fromChar(char c);
		}
	}

	public enum Short implements NetworkCodec<java.lang.Short> {
		INSTANCE;

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

		public <A> NetworkCodec<A> mapShort(ToShort<A> to, FromShort<A> from) {
			return new SimpleNetworkCodec<>(
					(byteBuf, a) -> this.encodeShort(byteBuf, to.toShort(a)),
					byteBuf -> from.fromShort(this.decodeShort(byteBuf))
			);
		}

		@Override
		public String toString() {
			return "Short";
		}

		public interface ToShort<A> {
			short toShort(A a);
		}

		public interface FromShort<A> {
			A fromShort(short s);
		}
	}

	public static class Int implements NetworkCodec<Integer> {
		public static final Int INSTANCE = new Int();

		private Int() { }

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

		public <A> NetworkCodec<A> mapInt(ToIntFunction<A> to, IntFunction<A> from) {
			return new SimpleNetworkCodec<>(
					(byteBuf, a) -> this.encodeInt(byteBuf, to.applyAsInt(a)),
					byteBuf -> from.apply(this.decodeInt(byteBuf))
			);
		}

		@Override
		public String toString() {
			return "Int";
		}
	}

	public static final class VarInt extends Int {
		public static final VarInt INSTANCE = new VarInt();
		private VarInt() { }

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

	public enum Float implements NetworkCodec<java.lang.Float> {
		INSTANCE;

		@Override
		public java.lang.Float decode(PacketByteBuf buf) {
			return this.decodeFloat(buf);
		}

		@Override
		public void encode(PacketByteBuf buf, java.lang.Float data) {
			this.encodeFloat(buf, data);
		}

		public float decodeFloat(PacketByteBuf buf) {
			return buf.readFloat();
		}

		public void encodeFloat(PacketByteBuf buf, float data) {
			buf.writeFloat(data);
		}

		public <A> NetworkCodec<A> mapFloat(ToFloatFunction<A> to, FromFloatFunction<A> from) {
			return new SimpleNetworkCodec<>(
					(byteBuf, a) -> this.encodeFloat(byteBuf, to.toFloat(a)),
					byteBuf -> from.fromFloat(this.decodeFloat(byteBuf))
			);
		}

		@Override
		public String toString() {
			return "Float";
		}

		public interface ToFloatFunction<A> {
			float toFloat(A a);
		}

		public interface FromFloatFunction<A> {
			A fromFloat(float f);
		}
	}

	public static class Long implements NetworkCodec<java.lang.Long> {
		public static final Long INSTANCE = new Long();

		private Long() { }

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

		public <A> NetworkCodec<A> mapLong(ToLongFunction<A> to, LongFunction<A> from) {
			return new SimpleNetworkCodec<>(
					(byteBuf, a) -> this.encodeLong(byteBuf, to.applyAsLong(a)),
					byteBuf -> from.apply(this.decodeLong(byteBuf))
			);
		}

		@Override
		public String toString() {
			return "Long";
		}
	}

	public static final class VarLong extends Long {
		public static final VarLong INSTANCE = new VarLong();

		private VarLong() { }

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

	public enum Double implements NetworkCodec<java.lang.Double> {
		INSTANCE;

		@Override
		public java.lang.Double decode(PacketByteBuf buf) {
			return this.decodeDouble(buf);
		}

		@Override
		public void encode(PacketByteBuf buf, java.lang.Double data) {
			this.encodeDouble(buf, data);
		}

		public double decodeDouble(PacketByteBuf buf) {
			return buf.readDouble();
		}

		public void encodeDouble(PacketByteBuf buf, double data) {
			buf.writeDouble(data);
		}

		public <A> NetworkCodec<A> mapDouble(ToDoubleFunction<A> to, DoubleFunction<A> from) {
			return new SimpleNetworkCodec<>(
					(byteBuf, a) -> this.encodeDouble(byteBuf, to.applyAsDouble(a)),
					byteBuf -> from.apply(this.decodeDouble(byteBuf))
			).named("Double [mapped]");
		}

		@Override
		public String toString() {
			return "Double";
		}
	}
}
