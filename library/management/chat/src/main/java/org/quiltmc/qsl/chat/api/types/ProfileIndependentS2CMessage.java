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

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.s2c.play.ProfileIndependentMessageS2CPacket;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.impl.InternalMessageTypesSupplier;

import java.util.EnumSet;

public class ProfileIndependentS2CMessage extends AbstractChatMessage<ProfileIndependentMessageS2CPacket> {
	private final Text message;
	private final MessageType.Parameters messageType;

	public ProfileIndependentS2CMessage(PlayerEntity player, boolean isClient, ProfileIndependentMessageS2CPacket packet) {
		this(
				player,
				isClient,
				packet.message(),
				packet.messageType().createParameters(player.world.getRegistryManager()).orElseGet(() -> {
					if (player instanceof ClientPlayerEntity clientPlayerEntity) {
						clientPlayerEntity.networkHandler.getConnection().disconnect(Text.translatable("multiplayer.disconnect.invalid_packet"));
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
		return InternalMessageTypesSupplier.s2cType(QuiltMessageType.PROFILE_INDEPENDENT, isClient);
	}

	@Override
	public @NotNull ProfileIndependentMessageS2CPacket serialized() {
		return new ProfileIndependentMessageS2CPacket(message, messageType.serialize(player.world.getRegistryManager()));
	}

	public Text getMessage() {
		return message;
	}

	public MessageType.Parameters getMessageType() {
		return messageType;
	}

	public ProfileIndependentS2CMessage withMessage(Text message) {
		return new ProfileIndependentS2CMessage(player, isClient, message, messageType);
	}

	public ProfileIndependentS2CMessage withMessageType(MessageType.Parameters messageType) {
		return new ProfileIndependentS2CMessage(player, isClient, message, messageType);
	}

	@Override
	public String toString() {
		return "ProfileIndependentS2CMessage{" + "message=" + message +
				", messageType=" + messageType +
				", player=" + player +
				", isClient=" + isClient +
				'}';
	}
}
