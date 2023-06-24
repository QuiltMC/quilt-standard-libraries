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

package org.quiltmc.qsl.chat.api.types;

import java.time.Instant;
import java.util.EnumSet;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.MessageSignature;
import net.minecraft.network.message.MessageSignatureList;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;

import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.impl.InternalMessageTypesFactory;

/**
 * A wrapper around a C2S chat message after it has been signed.
 *
 * @see RawChatC2SMessage
 */
public class ChatC2SMessage extends AbstractChatMessage<ChatMessageC2SPacket> {
	private final String message;
	private final Instant timestamp;
	private final long salt;
	private final @Nullable MessageSignature signature;
	private final MessageSignatureList.Acknowledgment messageAcknowledgments;

	public ChatC2SMessage(PlayerEntity player, boolean isClient, ChatMessageC2SPacket packet) {
		this(
				player,
				isClient,
				packet.message(),
				packet.timestamp(),
				packet.salt(),
				packet.signature(),
				packet.messageAcknowledgments()
		);
	}

	public ChatC2SMessage(PlayerEntity player, boolean isClient, String message, Instant timestamp, long salt, @Nullable MessageSignature signature, MessageSignatureList.Acknowledgment messageAcknowledgments) {
		super(player, isClient);
		this.message = message;
		this.timestamp = timestamp;
		this.salt = salt;
		this.signature = signature;
		this.messageAcknowledgments = messageAcknowledgments;
	}

	@Override
	public @NotNull EnumSet<QuiltMessageType> getTypes() {
		return InternalMessageTypesFactory.c2sType(QuiltMessageType.CHAT, this.isClient);
	}

	@Override
	public @NotNull ChatMessageC2SPacket serialized() {
		return new ChatMessageC2SPacket(this.message, this.timestamp, this.salt, this.signature, this.messageAcknowledgments);
	}

	@Contract(pure = true)
	public String getMessage() {
		return this.message;
	}

	@Contract(pure = true)
	public Instant getTimestamp() {
		return this.timestamp;
	}

	@Contract(pure = true)
	public long getSalt() {
		return this.salt;
	}

	@Contract(pure = true)
	public @Nullable MessageSignature getSignature() {
		return this.signature;
	}

	@Contract(pure = true)
	public MessageSignatureList.Acknowledgment getMessageAcknowledgments() {
		return this.messageAcknowledgments;
	}

	@Contract(value = "_ -> new", pure = true)
	public ChatC2SMessage withMessage(String message) {
		return new ChatC2SMessage(this.player, this.isClient, message, this.timestamp, this.salt, this.signature, this.messageAcknowledgments);
	}

	@Contract(value = "_ -> new", pure = true)
	public ChatC2SMessage withTimestamp(Instant timestamp) {
		return new ChatC2SMessage(this.player, this.isClient, this.message, timestamp, this.salt, this.signature, this.messageAcknowledgments);
	}

	@Contract(value = "_ -> new", pure = true)
	public ChatC2SMessage withSalt(long salt) {
		return new ChatC2SMessage(this.player, this.isClient, this.message, this.timestamp, salt, this.signature, this.messageAcknowledgments);
	}

	@Contract(value = "_ -> new", pure = true)
	public ChatC2SMessage withSignature(@Nullable MessageSignature signature) {
		return new ChatC2SMessage(this.player, this.isClient, this.message, this.timestamp, this.salt, signature, this.messageAcknowledgments);
	}

	@Contract(value = "_ -> new", pure = true)
	public ChatC2SMessage withMessageAcknowledgments(MessageSignatureList.Acknowledgment messageAcknowledgments) {
		return new ChatC2SMessage(this.player, this.isClient, this.message, this.timestamp, this.salt, this.signature, messageAcknowledgments);
	}

	@Override
	public String toString() {
		return "ChatC2SMessage{" + "message='" + this.message + '\'' +
				", timestamp=" + this.timestamp +
				", salt=" + this.salt +
				", signature=" + this.signature +
				", messageAcknowledgments=" + this.messageAcknowledgments +
				", player=" + this.player +
				", isClient=" + this.isClient +
				'}';
	}
}
