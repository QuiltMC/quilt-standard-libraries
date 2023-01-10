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

	public MutableC2SChatMessage(PlayerEntity player, ChatMessageC2SPacket packet) {
		this(
				player,
				packet.message(),
				packet.timestamp(),
				packet.salt(),
				packet.signature(),
				packet.messageAcknowledgments()
		);
	}
	public MutableC2SChatMessage(PlayerEntity player, String message, Instant timestamp, long salt, @Nullable MessageSignature signature, MessageSignatureList.Acknowledgment messageAcknowledgments) {
		super(player);
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
		return new ImmutableC2SChatMessage(player, message, timestamp, salt, signature, messageAcknowledgments);
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
}
