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

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.s2c.play.ProfileIndependentMessageS2CPacket;
import net.minecraft.text.Text;

import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.impl.InternalMessageTypesFactory;
import org.quiltmc.qsl.chat.mixin.client.ClientPlayNetworkHandlerAccessor;

/**
 * A wrapper around a "profile independent" message. These are usually created as a result of commands like {@link net.minecraft.server.command.MessageCommand}.
 */
public class ProfileIndependentS2CMessage extends AbstractChatMessage<ProfileIndependentMessageS2CPacket> {
	private final Text message;
	private final MessageType.Parameters messageType;

	public ProfileIndependentS2CMessage(PlayerEntity player, boolean isClient, ProfileIndependentMessageS2CPacket packet) {
		this(
				player,
				isClient,
				packet.message(),
				packet.messageType().createParameters(player.getWorld().getRegistryManager()).orElseGet(() -> {
					if (player instanceof ClientPlayerEntity clientPlayerEntity) {
						((ClientPlayNetworkHandlerAccessor) clientPlayerEntity.networkHandler).getConnection().disconnect(Text.translatable("multiplayer.disconnect.invalid_packet"));
					}

					return null;
				})
		);
	}

	public ProfileIndependentS2CMessage(PlayerEntity player, boolean isClient, Text message, MessageType.Parameters messageType) {
		super(player, isClient);
		this.message = message;
		this.messageType = messageType;
	}

	@Override
	public @NotNull EnumSet<QuiltMessageType> getTypes() {
		return InternalMessageTypesFactory.s2cType(QuiltMessageType.PROFILE_INDEPENDENT, this.isClient);
	}

	@Override
	public @NotNull ProfileIndependentMessageS2CPacket serialized() {
		return new ProfileIndependentMessageS2CPacket(this.message, this.messageType.serialize(this.player.getWorld().getRegistryManager()));
	}

	@Contract(pure = true)
	public Text getMessage() {
		return this.message;
	}

	@Contract(pure = true)
	public MessageType.Parameters getMessageType() {
		return this.messageType;
	}

	@Contract(value = "_ -> new", pure = true)
	public ProfileIndependentS2CMessage withMessage(Text message) {
		return new ProfileIndependentS2CMessage(this.player, this.isClient, message, this.messageType);
	}

	@Contract(value = "_ -> new", pure = true)
	public ProfileIndependentS2CMessage withMessageType(MessageType.Parameters messageType) {
		return new ProfileIndependentS2CMessage(this.player, this.isClient, this.message, messageType);
	}

	@Override
	public String toString() {
		return "ProfileIndependentS2CMessage{" + "message=" + this.message +
				", messageType=" + this.messageType +
				", player=" + this.player +
				", isClient=" + this.isClient +
				'}';
	}
}
