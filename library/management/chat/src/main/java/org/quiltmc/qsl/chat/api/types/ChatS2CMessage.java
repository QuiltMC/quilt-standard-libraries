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
import net.minecraft.network.message.FilterMask;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageSignature;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.impl.InternalQuiltChatApiUtil;

import java.util.EnumSet;
import java.util.UUID;

public class ChatS2CMessage extends AbstractChatMessage<ChatMessageS2CPacket> {
	private final UUID sender;
	private final int index;
	private final MessageSignature signature;
	private final MessageBody.Serialized body;
	private final Text unsignedContent;
	private final FilterMask filterMask;
	private final MessageType.Serialized messageType;

	public ChatS2CMessage(PlayerEntity player, boolean isOnClientSide, ChatMessageS2CPacket packet) {
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

	public ChatS2CMessage(PlayerEntity player, boolean isOnClientSide, UUID sender, int index, MessageSignature signature, MessageBody.Serialized body, Text unsignedContent, FilterMask filterMask, MessageType.Serialized messageType) {
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
		return InternalQuiltChatApiUtil.s2cType(QuiltMessageType.CHAT, isOnClientSide);
	}

	@Override
	public @NotNull ChatMessageS2CPacket serialized() {
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

	public ChatS2CMessage withSender(UUID sender) {
		return new ChatS2CMessage(player, isOnClientSide, sender, index, signature, body, unsignedContent, filterMask, messageType);
	}

	public ChatS2CMessage withIndex(int index) {
		return new ChatS2CMessage(player, isOnClientSide, sender, index, signature, body, unsignedContent, filterMask, messageType);
	}

	public ChatS2CMessage withSignature(MessageSignature signature) {
		return new ChatS2CMessage(player, isOnClientSide, sender, index, signature, body, unsignedContent, filterMask, messageType);
	}

	public ChatS2CMessage withBody(MessageBody.Serialized body) {
		return new ChatS2CMessage(player, isOnClientSide, sender, index, signature, body, unsignedContent, filterMask, messageType);
	}

	public ChatS2CMessage withUnsignedContent(Text unsignedContent) {
		return new ChatS2CMessage(player, isOnClientSide, sender, index, signature, body, unsignedContent, filterMask, messageType);
	}

	public ChatS2CMessage withFilterMask(FilterMask filterMask) {
		return new ChatS2CMessage(player, isOnClientSide, sender, index, signature, body, unsignedContent, filterMask, messageType);
	}

	public ChatS2CMessage withMessageType(MessageType.Serialized messageType) {
		return new ChatS2CMessage(player, isOnClientSide, sender, index, signature, body, unsignedContent, filterMask, messageType);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ChatS2CMessage{");
		sb.append("sender=").append(sender);
		sb.append(", index=").append(index);
		sb.append(", signature=").append(signature);
		sb.append(", body=").append(body);
		sb.append(", unsignedContent=").append(unsignedContent);
		sb.append(", filterMask=").append(filterMask);
		sb.append(", messageType=").append(messageType);
		sb.append(", player=").append(player);
		sb.append(", isOnClientSide=").append(isOnClientSide);
		sb.append('}');
		return sb.toString();
	}
}
