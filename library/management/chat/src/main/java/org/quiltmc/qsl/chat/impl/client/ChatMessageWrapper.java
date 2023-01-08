package org.quiltmc.qsl.chat.impl.client;

import net.minecraft.network.message.FilterMask;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageSignature;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.UUID;

@ClientOnly
public class ChatMessageWrapper {
	private UUID sender;
	private int index;
	private MessageSignature signature;
	private MessageBody.Serialized body;
	private Text unsignedContent;
	private FilterMask filterMask;
	private MessageType.Serialized chatType;

	public ChatMessageWrapper(ChatMessageS2CPacket packet) {
		this(
				packet.sender(),
				packet.index(),
				packet.signature(),
				packet.body(),
				packet.unsignedContent(),
				packet.filterMask(),
				packet.messageType()
		);
	}
	private ChatMessageWrapper(
			UUID sender,
			int index,
			@Nullable MessageSignature signature,
			MessageBody.Serialized body,
			@Nullable Text unsignedContent,
			FilterMask filterMask,
			MessageType.Serialized chatType
	) {

		this.sender = sender;
		this.index = index;
		this.signature = signature;
		this.body = body;
		this.unsignedContent = unsignedContent;
		this.filterMask = filterMask;
		this.chatType = chatType;
	}

	public ChatMessageS2CPacket asPacket() {
		return new ChatMessageS2CPacket(
				sender,
				index,
				signature,
				body,
				unsignedContent,
				filterMask,
				chatType
		);
	}

	public UUID getSender() {
		return sender;
	}

	public void setSender(UUID sender) {
		this.sender = sender;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public MessageSignature getSignature() {
		return signature;
	}

	public void setSignature(MessageSignature signature) {
		this.signature = signature;
	}

	public MessageBody.Serialized getBody() {
		return body;
	}

	public void setBody(MessageBody.Serialized body) {
		this.body = body;
	}

	public Text getUnsignedContent() {
		return unsignedContent;
	}

	public void setUnsignedContent(Text unsignedContent) {
		this.unsignedContent = unsignedContent;
	}

	public FilterMask getFilterMask() {
		return filterMask;
	}

	public void setFilterMask(FilterMask filterMask) {
		this.filterMask = filterMask;
	}

	public MessageType.Serialized getChatType() {
		return chatType;
	}

	public void setChatType(MessageType.Serialized chatType) {
		this.chatType = chatType;
	}
}
