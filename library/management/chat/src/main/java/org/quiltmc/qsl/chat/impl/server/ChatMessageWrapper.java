package org.quiltmc.qsl.chat.impl.server;

import net.minecraft.network.message.MessageSignature;
import net.minecraft.network.message.MessageSignatureList;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;

public class ChatMessageWrapper {
	private String message;
	private Instant timestamp;
	private long salt;

	@Nullable
	private MessageSignature signature;
	private MessageSignatureList.Acknowledgment messageAcknowledgments;

	public ChatMessageWrapper(ChatMessageC2SPacket packet) {
		this(
				packet.message(),
				packet.timestamp(),
				packet.salt(),
				packet.signature(),
				packet.messageAcknowledgments()
		);
	}

	public ChatMessageWrapper(
			String message,
			Instant timestamp,
			long salt,
			@Nullable MessageSignature signature,
			MessageSignatureList.Acknowledgment messageAcknowledgments
	) {
		this.message = message;
		this.timestamp = timestamp;
		this.salt = salt;
		this.signature = signature;
		this.messageAcknowledgments = messageAcknowledgments;
	}

	public ChatMessageC2SPacket asPacket() {
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

	public void setTimestamp(Instant timeStamp) {
		this.timestamp = timeStamp;
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

	public void setMessageAcknowledgments(MessageSignatureList.Acknowledgment lastSeenMessages) {
		this.messageAcknowledgments = lastSeenMessages;
	}
}
