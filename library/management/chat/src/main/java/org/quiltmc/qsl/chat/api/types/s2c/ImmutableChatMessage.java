package org.quiltmc.qsl.chat.api.types.s2c;

import net.minecraft.network.message.MessageSignature;
import net.minecraft.network.message.MessageSignatureList;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.ImmutableAbstractMessage;

import java.time.Instant;
import java.util.EnumSet;

public class ImmutableChatMessage extends ImmutableAbstractMessage<ImmutableChatMessage, ChatMessageC2SPacket> {
	private final ServerPlayNetworkHandler networkHandler;
	private final String message;
	private final Instant timestamp;
	private final long salt;
	private final @Nullable MessageSignature signature;
	private final MessageSignatureList.Acknowledgment messageAcknowledgments;

	public ImmutableChatMessage(ServerPlayNetworkHandler networkHandler, ChatMessageC2SPacket packet) {
		this(
				networkHandler,
				packet.message(),
				packet.timestamp(),
				packet.salt(),
				packet.signature(),
				packet.messageAcknowledgments()
		);
	}

	public ImmutableChatMessage(ServerPlayNetworkHandler networkHandler, String message, Instant timestamp, long salt, @Nullable MessageSignature signature, MessageSignatureList.Acknowledgment messageAcknowledgments) {
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
