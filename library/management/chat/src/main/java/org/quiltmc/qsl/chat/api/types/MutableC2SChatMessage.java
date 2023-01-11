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

import java.time.Instant;
import java.util.EnumSet;

public final class MutableC2SChatMessage extends MutableAbstractMessage<ImmutableC2SChatMessage, ChatMessageC2SPacket> {
	private String message;
	private Instant timestamp;
	private long salt;
	private @Nullable MessageSignature signature;
	private MessageSignatureList.Acknowledgment messageAcknowledgments;

	public MutableC2SChatMessage(PlayerEntity player, boolean isOnClientSide, ChatMessageC2SPacket packet) {
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
	public MutableC2SChatMessage(PlayerEntity player, boolean isOnClientSide, String message, Instant timestamp, long salt, @Nullable MessageSignature signature, MessageSignatureList.Acknowledgment messageAcknowledgments) {
		super(player, isOnClientSide);
		this.message = message;
		this.timestamp = timestamp;
		this.salt = salt;
		this.signature = signature;
		this.messageAcknowledgments = messageAcknowledgments;
	}

	@Override
	public @NotNull EnumSet<QuiltMessageType> getTypes() {
		return EnumSet.of(QuiltMessageType.CHAT, QuiltMessageType.CLIENT, QuiltMessageType.OUTBOUND);
	}

	@Override
	public @NotNull ImmutableC2SChatMessage immutableCopy() {
		return new ImmutableC2SChatMessage(player, isOnClientSide, message, timestamp, salt, signature, messageAcknowledgments);
	}

	@Override
	public @NotNull ChatMessageC2SPacket asPacket() {
		return new ChatMessageC2SPacket(message, timestamp, salt, signature, messageAcknowledgments);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Instant timestamp) {
		this.timestamp = timestamp;
	}

	public long getSalt() {
		return salt;
	}

	public void setSalt(long salt) {
		this.salt = salt;
	}

	public @Nullable MessageSignature getSignature() {
		return signature;
	}

	public void setSignature(@Nullable MessageSignature signature) {
		this.signature = signature;
	}

	public MessageSignatureList.Acknowledgment getMessageAcknowledgments() {
		return messageAcknowledgments;
	}

	public void setMessageAcknowledgments(MessageSignatureList.Acknowledgment messageAcknowledgments) {
		this.messageAcknowledgments = messageAcknowledgments;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("MutableC2SChatMessage{");
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
