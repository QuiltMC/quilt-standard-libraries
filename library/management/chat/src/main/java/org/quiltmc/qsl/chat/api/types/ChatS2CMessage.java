/*
 * Copyright 2023 The Quilt Project
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

import java.util.EnumSet;
import java.util.UUID;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.FilterMask;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageSignature;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.text.Text;

import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.impl.InternalMessageTypesFactory;

/**
 * A wrapper around an S2C chat message. These are chat messages from players being relayed from the server.
 */
public class ChatS2CMessage extends AbstractChatMessage<ChatMessageS2CPacket> {
	private final UUID sender;
	private final int index;
	private final MessageSignature signature;
	private final MessageBody.Serialized body;
	private final Text unsignedContent;
	private final FilterMask filterMask;
	private final MessageType.Serialized messageType;

	public ChatS2CMessage(PlayerEntity player, boolean isClient, ChatMessageS2CPacket packet) {
		this(
				player, isClient,
				packet.sender(),
				packet.index(),
				packet.signature(),
				packet.body(),
				packet.unsignedContent(),
				packet.filterMask(),
				packet.messageType()
		);
	}

	public ChatS2CMessage(PlayerEntity player, boolean isClient, UUID sender, int index, MessageSignature signature, MessageBody.Serialized body, Text unsignedContent, FilterMask filterMask, MessageType.Serialized messageType) {
		super(player, isClient);
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
		return InternalMessageTypesFactory.s2cType(QuiltMessageType.CHAT, this.isClient);
	}

	@Override
	public @NotNull ChatMessageS2CPacket serialized() {
		return new ChatMessageS2CPacket(this.sender, this.index, this.signature, this.body, this.unsignedContent, this.filterMask, this.messageType);
	}

	@Contract(pure = true)
	public UUID getSender() {
		return this.sender;
	}

	@Contract(pure = true)
	public int getIndex() {
		return this.index;
	}

	@Contract(pure = true)
	public MessageSignature getSignature() {
		return this.signature;
	}

	@Contract(pure = true)
	public MessageBody.Serialized getBody() {
		return this.body;
	}

	@Contract(pure = true)
	public Text getUnsignedContent() {
		return this.unsignedContent;
	}

	@Contract(pure = true)
	public FilterMask getFilterMask() {
		return this.filterMask;
	}

	@Contract(pure = true)
	public MessageType.Serialized getMessageType() {
		return this.messageType;
	}

	@Contract(value = "_ -> new", pure = true)
	public ChatS2CMessage withSender(UUID sender) {
		return new ChatS2CMessage(this.player, this.isClient, sender, this.index, this.signature, this.body, this.unsignedContent, this.filterMask, this.messageType);
	}

	@Contract(value = "_ -> new", pure = true)
	public ChatS2CMessage withIndex(int index) {
		return new ChatS2CMessage(this.player, this.isClient, this.sender, index, this.signature, this.body, this.unsignedContent, this.filterMask, this.messageType);
	}

	@Contract(value = "_ -> new", pure = true)
	public ChatS2CMessage withSignature(MessageSignature signature) {
		return new ChatS2CMessage(this.player, this.isClient, this.sender, this.index, signature, this.body, this.unsignedContent, this.filterMask, this.messageType);
	}

	@Contract(value = "_ -> new", pure = true)
	public ChatS2CMessage withBody(MessageBody.Serialized body) {
		return new ChatS2CMessage(this.player, this.isClient, this.sender, this.index, this.signature, body, this.unsignedContent, this.filterMask, this.messageType);
	}

	@Contract(value = "_ -> new", pure = true)
	public ChatS2CMessage withUnsignedContent(Text unsignedContent) {
		return new ChatS2CMessage(this.player, this.isClient, this.sender, this.index, this.signature, this.body, unsignedContent, this.filterMask, this.messageType);
	}

	@Contract(value = "_ -> new", pure = true)
	public ChatS2CMessage withFilterMask(FilterMask filterMask) {
		return new ChatS2CMessage(this.player, this.isClient, this.sender, this.index, this.signature, this.body, this.unsignedContent, filterMask, this.messageType);
	}

	@Contract(value = "_ -> new", pure = true)
	public ChatS2CMessage withMessageType(MessageType.Serialized messageType) {
		return new ChatS2CMessage(this.player, this.isClient, this.sender, this.index, this.signature, this.body, this.unsignedContent, this.filterMask, messageType);
	}

	@Override
	public String toString() {
		return "ChatS2CMessage{" + "sender=" + this.sender +
				", index=" + this.index +
				", signature=" + this.signature +
				", body=" + this.body +
				", unsignedContent=" + this.unsignedContent +
				", filterMask=" + this.filterMask +
				", messageType=" + this.messageType +
				", player=" + this.player +
				", isClient=" + this.isClient +
				'}';
	}
}
