/*
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

package org.quiltmc.qsl.networking.api;

import java.util.Objects;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import net.minecraft.network.PacketByteBuf;

/**
 * Helper methods for working with and creating {@link PacketByteBuf}s.
 */
public final class PacketByteBufs {
	private static final PacketByteBuf EMPTY_PACKET_BYTE_BUF = new PacketByteBuf(Unpooled.EMPTY_BUFFER);

	/**
	 * Returns an empty instance of packet byte buffer.
	 *
	 * @return an empty buffer
	 */
	public static PacketByteBuf empty() {
		return EMPTY_PACKET_BYTE_BUF;
	}

	/**
	 * Returns a new heap memory-backed instance of packet byte buffer.
	 *
	 * @return a new buffer
	 */
	public static PacketByteBuf create() {
		return new PacketByteBuf(Unpooled.buffer());
	}

	// Convenience methods for byte buffer methods that return a new byte buffer

	/**
	 * Wraps the newly created buf from {@code buf.readBytes} in a packet byte buffer.
	 *
	 * @param buf    the original buffer
	 * @param length the number of bytes to transfer
	 * @return the transferred bytes
	 * @see ByteBuf#readBytes(int)
	 */
	public static PacketByteBuf readBytes(ByteBuf buf, int length) {
		Objects.requireNonNull(buf, "ByteBuf cannot be null");

		return new PacketByteBuf(buf.readBytes(length));
	}

	/**
	 * Wraps the newly created buffer from {@code buf.readSlice} in a packet byte buffer.
	 *
	 * @param buf    the original buffer
	 * @param length the size of the new slice
	 * @return the newly created slice
	 * @see ByteBuf#readSlice(int)
	 */
	public static PacketByteBuf readSlice(ByteBuf buf, int length) {
		Objects.requireNonNull(buf, "ByteBuf cannot be null");

		return new PacketByteBuf(buf.readSlice(length));
	}

	/**
	 * Wraps the newly created buffer from {@code buf.readRetainedSlice} in a packet byte buffer.
	 *
	 * @param buf    the original buffer
	 * @param length the size of the new slice
	 * @return the newly created slice
	 * @see ByteBuf#readRetainedSlice(int)
	 */
	public static PacketByteBuf readRetainedSlice(ByteBuf buf, int length) {
		Objects.requireNonNull(buf, "ByteBuf cannot be null");

		return new PacketByteBuf(buf.readRetainedSlice(length));
	}

	/**
	 * Wraps the newly created buffer from {@code buf.copy} in a packet byte buffer.
	 *
	 * @param buf the original buffer
	 * @return a copy of the buffer
	 * @see ByteBuf#copy()
	 */
	public static PacketByteBuf copy(ByteBuf buf) {
		Objects.requireNonNull(buf, "ByteBuf cannot be null");

		return new PacketByteBuf(buf.copy());
	}

	/**
	 * Wraps the newly created buffer from {@code buf.copy} in a packet byte buffer.
	 *
	 * @param buf    the original buffer
	 * @param index  the starting index
	 * @param length the size of the copy
	 * @return a copy of the buffer
	 * @see ByteBuf#copy(int, int)
	 */
	public static PacketByteBuf copy(ByteBuf buf, int index, int length) {
		Objects.requireNonNull(buf, "ByteBuf cannot be null");

		return new PacketByteBuf(buf.copy(index, length));
	}

	/**
	 * Wraps the newly created buffer from {@code buf.slice} in a packet byte buffer.
	 *
	 * @param buf the original buffer
	 * @return a slice of the buffer
	 * @see ByteBuf#slice()
	 */
	public static PacketByteBuf slice(ByteBuf buf) {
		Objects.requireNonNull(buf, "ByteBuf cannot be null");

		return new PacketByteBuf(buf.slice());
	}

	/**
	 * Wraps the newly created buffer from {@code buf.retainedSlice} in a packet byte buffer.
	 *
	 * @param buf the original buffer
	 * @return a slice of the buffer
	 * @see ByteBuf#retainedSlice()
	 */
	public static PacketByteBuf retainedSlice(ByteBuf buf) {
		Objects.requireNonNull(buf, "ByteBuf cannot be null");

		return new PacketByteBuf(buf.retainedSlice());
	}

	/**
	 * Wraps the newly created buffer from {@code buf.slice} in a packet byte buffer.
	 *
	 * @param buf    the original buffer
	 * @param index  the starting index
	 * @param length the size of the copy
	 * @return a slice of the buffer
	 * @see ByteBuf#slice(int, int)
	 */
	public static PacketByteBuf slice(ByteBuf buf, int index, int length) {
		Objects.requireNonNull(buf, "ByteBuf cannot be null");

		return new PacketByteBuf(buf.slice(index, length));
	}

	/**
	 * Wraps the newly created buffer from {@code buf.retainedSlice} in a packet byte buffer.
	 *
	 * @param buf    the original buffer
	 * @param index  the starting index
	 * @param length the size of the copy
	 * @return a slice of the buffer
	 * @see ByteBuf#retainedSlice(int, int)
	 */
	public static PacketByteBuf retainedSlice(ByteBuf buf, int index, int length) {
		Objects.requireNonNull(buf, "ByteBuf cannot be null");

		return new PacketByteBuf(buf.retainedSlice(index, length));
	}

	/**
	 * Wraps the newly created buffer from {@code buf.duplicate} in a packet byte buffer.
	 *
	 * @param buf the original buffer
	 * @return a duplicate of the buffer
	 * @see ByteBuf#duplicate()
	 */
	public static PacketByteBuf duplicate(ByteBuf buf) {
		Objects.requireNonNull(buf, "ByteBuf cannot be null");

		return new PacketByteBuf(buf.duplicate());
	}

	/**
	 * Wraps the newly created buffer from {@code buf.retainedDuplicate} in a packet byte buffer.
	 *
	 * @param buf the original buffer
	 * @return a duplicate of the buffer
	 * @see ByteBuf#retainedDuplicate()
	 */
	public static PacketByteBuf retainedDuplicate(ByteBuf buf) {
		Objects.requireNonNull(buf, "ByteBuf cannot be null");

		return new PacketByteBuf(buf.retainedDuplicate());
	}

	private PacketByteBufs() {
	}
}
