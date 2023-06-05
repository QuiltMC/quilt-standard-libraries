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

import java.time.Instant;
import java.util.EnumSet;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.impl.InternalMessageTypesFactory;

import net.minecraft.command.argument.ArgumentSignatures;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.MessageSignatureList;
import net.minecraft.network.packet.c2s.play.ChatCommandC2SPacket;

public class CommandC2SMessage extends AbstractChatMessage<ChatCommandC2SPacket> {
	private final String command;
	private final Instant timestamp;
	private final long salt;
	private final ArgumentSignatures argumentSignatures;
	private final MessageSignatureList.Acknowledgment messageAcknowledgements;

	public CommandC2SMessage(PlayerEntity player, boolean isClient, ChatCommandC2SPacket packet) {
		this(
				player,
				isClient,
				packet.command(),
				packet.timestamp(),
				packet.salt(),
				packet.argumentSignatures(),
				packet.messageAcknowledgements()
		);
	}

	public CommandC2SMessage(PlayerEntity player, boolean isClient, String command, Instant timestamp, long salt, ArgumentSignatures argumentSignatures, MessageSignatureList.Acknowledgment messageAcknowledgements) {
		super(player, isClient);
		this.command = command;
		this.timestamp = timestamp;
		this.salt = salt;
		this.argumentSignatures = argumentSignatures;
		this.messageAcknowledgements = messageAcknowledgements;
	}

	@Override
	public @NotNull EnumSet<QuiltMessageType> getTypes() {
		return InternalMessageTypesFactory.c2sType(QuiltMessageType.COMMAND, isClient);
	}

	public String getCommand() {
		return command;
	}

	public Instant getTimestamp() {
		return timestamp;
	}

	public long getSalt() {
		return salt;
	}

	public ArgumentSignatures getArgumentSignatures() {
		return argumentSignatures;
	}

	public MessageSignatureList.Acknowledgment getMessageAcknowledgements() {
		return messageAcknowledgements;
	}

	@Contract(value = "_ -> new", pure = true)
	public CommandC2SMessage withCommand(String command) {
		return new CommandC2SMessage(this.player, this.isClient, command, this.timestamp, this.salt, this.argumentSignatures, this.messageAcknowledgements);
	}

	@Contract(value = "_ -> new", pure = true)
	public CommandC2SMessage withTimestamp(Instant timestamp) {
		return new CommandC2SMessage(this.player, this.isClient, this.command, timestamp, this.salt, this.argumentSignatures, this.messageAcknowledgements);
	}

	@Contract(value = "_ -> new", pure = true)
	public CommandC2SMessage withSalt(long salt) {
		return new CommandC2SMessage(this.player, this.isClient, this.command, this.timestamp, salt, this.argumentSignatures, this.messageAcknowledgements);
	}

	@Contract(value = "_ -> new", pure = true)
	public CommandC2SMessage withArgumentSignatures(ArgumentSignatures argumentSignatures) {
		return new CommandC2SMessage(this.player, this.isClient, this.command, this.timestamp, this.salt, argumentSignatures, this.messageAcknowledgements);
	}

	@Contract(value = "_ -> new", pure = true)
	public CommandC2SMessage withMessageAcknowledgements(MessageSignatureList.Acknowledgment messageAcknowledgements) {
		return new CommandC2SMessage(this.player, this.isClient, this.command, this.timestamp, this.salt, this.argumentSignatures, messageAcknowledgements);
	}

	@Override
	public @NotNull ChatCommandC2SPacket serialized() {
		return new ChatCommandC2SPacket(command, timestamp, salt, argumentSignatures, messageAcknowledgements);
	}

	@Override
	public String toString() {
		return "CommandC2SMessage{" +
			"command='" + command + '\'' +
			", timestamp=" + timestamp +
			", salt=" + salt +
			", argumentSignatures=" + argumentSignatures +
			", messageAcknowledgements=" + messageAcknowledgements +
			", player=" + player +
			", isClient=" + isClient +
			'}';
	}
}
