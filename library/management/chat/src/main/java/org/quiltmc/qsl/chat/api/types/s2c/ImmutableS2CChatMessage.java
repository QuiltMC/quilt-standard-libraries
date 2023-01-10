package org.quiltmc.qsl.chat.api.types.s2c;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.FilterMask;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageSignature;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.ImmutableAbstractMessage;

import java.util.EnumSet;
import java.util.UUID;

public class ImmutableS2CChatMessage extends ImmutableAbstractMessage<ImmutableS2CChatMessage, ChatMessageS2CPacket> {
	private final UUID sender;
	private final int index;
	private final MessageSignature signature;
	private final MessageBody.Serialized body;
	private final Text unsignedContent;
	private final FilterMask filterMask;
	private final MessageType.Serialized messageType;

	public ImmutableS2CChatMessage(PlayerEntity player, boolean isOnClientSide, ChatMessageS2CPacket packet) {
		this(
				player, isOnClientSide,
				packet.sender(),
				packet.index(),
				packet.signature(),
				packet.body(),
				packet.unsignedContent(),
				packet.filterMask(),
				packet.messageType()
		);
	}

	public ImmutableS2CChatMessage(PlayerEntity player, boolean isOnClientSide, UUID sender, int index, MessageSignature signature, MessageBody.Serialized body, Text unsignedContent, FilterMask filterMask, MessageType.Serialized messageType) {
		super(player, isOnClientSide);
		this.sender = sender;
		this.index = index;
		this.signature = signature;
		this.body = body;
		this.unsignedContent = unsignedContent;
		this.filterMask = filterMask;
		this.messageType = messageType;
	}

	@Override
	public @NotNull EnumSet<QuiltMessageType> getTypes() {
		if (isOnClientSide) {
			return EnumSet.of(QuiltMessageType.CHAT, QuiltMessageType.CLIENT, QuiltMessageType.OUTBOUND);
		} else {
			return EnumSet.of(QuiltMessageType.CHAT, QuiltMessageType.SERVER, QuiltMessageType.INBOUND);
		}
	}

	@Override
	public @NotNull ImmutableS2CChatMessage immutableCopy() {
		return new ImmutableS2CChatMessage(player, isOnClientSide, sender, index, signature, body, unsignedContent, filterMask, messageType);
	}

	@Override
	public @NotNull ChatMessageS2CPacket asPacket() {
		return new ChatMessageS2CPacket(sender, index, signature, body, unsignedContent, filterMask, messageType);
	}

	public UUID getSender() {
		return sender;
	}

	public int getIndex() {
		return index;
	}

	public MessageSignature getSignature() {
		return signature;
	}

	public MessageBody.Serialized getBody() {
		return body;
	}

	public Text getUnsignedContent() {
		return unsignedContent;
	}

	public FilterMask getFilterMask() {
		return filterMask;
	}

	public MessageType.Serialized getMessageType() {
		return messageType;
	}
}
