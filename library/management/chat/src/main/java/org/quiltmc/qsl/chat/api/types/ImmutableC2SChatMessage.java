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

public final class ImmutableC2SChatMessage extends ImmutableAbstractMessage<ImmutableC2SChatMessage, ChatMessageC2SPacket> {
	private final String message;
	private final Instant timestamp;
	private final long salt;
	private final @Nullable MessageSignature signature;
	private final MessageSignatureList.Acknowledgment messageAcknowledgments;

	public ImmutableC2SChatMessage(PlayerEntity player, boolean isOnClientSide, ChatMessageC2SPacket packet) {
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

	public ImmutableC2SChatMessage(PlayerEntity player, boolean isOnClientSide, String message, Instant timestamp, long salt, @Nullable MessageSignature signature, MessageSignatureList.Acknowledgment messageAcknowledgments) {
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
}
