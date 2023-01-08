package org.quiltmc.qsl.chat.impl.client;

import net.minecraft.network.MessageType;
import net.minecraft.network.chat.MessageSignature;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.text.Text;
import net.minecraft.unmapped.C_bowvajrs;
import net.minecraft.unmapped.C_ogacucnf;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import java.util.UUID;

@ClientOnly
public class ChatMessageWrapper {
	private UUID sender;
	private int index;
	private MessageSignature signature;
	private C_ogacucnf.C_zneglxow body;
	private Text unsignedContent;
	private C_bowvajrs filterMask;
	private MessageType.C_xfxprdln chatType;

	public ChatMessageWrapper(ChatMessageS2CPacket packet) {
		this(
				packet.sender(),
				packet.index(),
				packet.signature(),
				packet.body(),
				packet.unsignedContent(),
				packet.filterMask(),
				packet.chatType()
		);
	}
	private ChatMessageWrapper(
			UUID sender,
			int index,
			@Nullable MessageSignature signature,
			C_ogacucnf.C_zneglxow body,
			@Nullable Text unsignedContent,
			C_bowvajrs filterMask,
			MessageType.C_xfxprdln chatType
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

	public C_ogacucnf.C_zneglxow getBody() {
		return body;
	}

	public void setBody(C_ogacucnf.C_zneglxow body) {
		this.body = body;
	}

	public Text getUnsignedContent() {
		return unsignedContent;
	}

	public void setUnsignedContent(Text unsignedContent) {
		this.unsignedContent = unsignedContent;
	}

	public C_bowvajrs getFilterMask() {
		return filterMask;
	}

	public void setFilterMask(C_bowvajrs filterMask) {
		this.filterMask = filterMask;
	}

	public MessageType.C_xfxprdln getChatType() {
		return chatType;
	}

	public void setChatType(MessageType.C_xfxprdln chatType) {
		this.chatType = chatType;
	}
}
