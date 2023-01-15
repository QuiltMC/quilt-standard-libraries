/*
 * Copyright 2023 QuiltMC
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

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.MessageSignature;
import net.minecraft.network.message.MessageSignatureList;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.impl.InternalQuiltChatApiUtil;

import java.time.Instant;
import java.util.EnumSet;

public class ChatC2SMessage extends AbstractChatMessage<ChatMessageC2SPacket> {
	private final String message;
	private final Instant timestamp;
	private final long salt;
	private final @Nullable MessageSignature signature;
	private final MessageSignatureList.Acknowledgment messageAcknowledgments;

	public ChatC2SMessage(PlayerEntity player, boolean isOnClientSide, ChatMessageC2SPacket packet) {
		this(
				player,
				isOnClientSide,
				packet.message(),
				packet.timestamp(),
				packet.salt(),
				packet.signature(),
				packet.messageAcknowledgments()
		);
	}

	public ChatC2SMessage(PlayerEntity player, boolean isOnClientSide, String message, Instant timestamp, long salt, @Nullable MessageSignature signature, MessageSignatureList.Acknowledgment messageAcknowledgments) {
		super(player, isOnClientSide);
		this.message = message;
		this.timestamp = timestamp;
		this.salt = salt;
		this.signature = signature;
		this.messageAcknowledgments = messageAcknowledgments;
	}

	@Override
	public @NotNull EnumSet<QuiltMessageType> getTypes() {
		return InternalQuiltChatApiUtil.c2sType(QuiltMessageType.CHAT, true);
	}

	@Override
	public @NotNull ChatMessageC2SPacket serialized() {
		return new ChatMessageC2SPacket(message, timestamp, salt, signature, messageAcknowledgments);
	}

	public String getMessage() {
		return message;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public long getSalt() {
		return salt;
	}

	public @Nullable MessageSignature getSignature() {
		return signature;
	}

	public MessageSignatureList.Acknowledgment getMessageAcknowledgments() {
		return messageAcknowledgments;
	}

	public ChatC2SMessage withMessage(String message) {
		return new ChatC2SMessage(player, isOnClientSide, message, timestamp, salt, signature, messageAcknowledgments);
	}

	public ChatC2SMessage withTimestamp(Instant timestamp) {
		return new ChatC2SMessage(player, isOnClientSide, message, timestamp, salt, signature, messageAcknowledgments);
	}

	public ChatC2SMessage withSalt(long salt) {
		return new ChatC2SMessage(player, isOnClientSide, message, timestamp, salt, signature, messageAcknowledgments);
	}

	public ChatC2SMessage withSignature(@Nullable MessageSignature signature) {
		return new ChatC2SMessage(player, isOnClientSide, message, timestamp, salt, signature, messageAcknowledgments);
	}

	public ChatC2SMessage withMessageAcknowledgments(MessageSignatureList.Acknowledgment messageAcknowledgments) {
		return new ChatC2SMessage(player, isOnClientSide, message, timestamp, salt, signature, messageAcknowledgments);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ChatC2SMessage{");
		sb.append("message='").append(message).append('\'');
		sb.append(", timestamp=").append(timestamp);
		sb.append(", salt=").append(salt);
		sb.append(", signature=").append(signature);
		sb.append(", messageAcknowledgments=").append(messageAcknowledgments);
		sb.append(", player=").append(player);
		sb.append(", isOnClientSide=").append(isOnClientSide);
		sb.append('}');
		return sb.toString();
	}
}
