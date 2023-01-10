package org.quiltmc.qsl.chat.api.types.s2c;

import net.minecraft.network.message.MessageSignature;
import net.minecraft.network.message.MessageSignatureList;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.MutableAbstractMessage;

import java.time.Instant;
import java.util.EnumSet;

public class MutableChatMessage extends MutableAbstractMessage<ImmutableChatMessage, ChatMessageC2SPacket> {
	private final ServerPlayNetworkHandler networkHandler;
	private String message;
	private Instant timestamp;
	private long salt;
	private @Nullable MessageSignature signature;
	private MessageSignatureList.Acknowledgment messageAcknowledgments;

	public MutableChatMessage(ServerPlayNetworkHandler networkHandler, ChatMessageC2SPacket packet) {
		this(
				networkHandler,
				packet.message(),
				packet.timestamp(),
				packet.salt(),
				packet.signature(),
				packet.messageAcknowledgments()
		);
	}
	public MutableChatMessage(ServerPlayNetworkHandler networkHandler, String message, Instant timestamp, long salt, @Nullable MessageSignature signature, MessageSignatureList.Acknowledgment messageAcknowledgments) {
		this.networkHandler = networkHandler;
		this.message = message;
		this.timestamp = timestamp;
		this.salt = salt;
		this.signature = signature;
		this.messageAcknowledgments = messageAcknowledgments;
	}

	@Override
	public @NotNull EnumSet<QuiltMessageType> getTypes() {
		return EnumSet.of(QuiltMessageType.CHAT, QuiltMessageType.SERVER, QuiltMessageType.INBOUND);
	}

	@Override
	public @NotNull ImmutableChatMessage immutableCopy() {
		return new ImmutableChatMessage(networkHandler, message, timestamp, salt, signature, messageAcknowledgments);
	}

	@Override
	public @NotNull ChatMessageC2SPacket packet() {
		return new ChatMessageC2SPacket(message, timestamp, salt, signature, messageAcknowledgments);
	}

	public ServerPlayNetworkHandler getNetworkHandler() {
		return networkHandler;
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
