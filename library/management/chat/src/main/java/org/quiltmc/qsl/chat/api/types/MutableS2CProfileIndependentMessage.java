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

import java.util.EnumSet;

public class MutableS2CProfileIndependentMessage extends MutableAbstractMessage<ImmutableS2CProfileIndependentMessage, ProfileIndependentMessageS2CPacket> {
	private Text message;
	private MessageType.Parameters messageType;

	public MutableS2CProfileIndependentMessage(PlayerEntity player, boolean isOnClient, ProfileIndependentMessageS2CPacket packet) {
		this(
				player,
				isOnClient,
				packet.message(),
				packet.messageType().createParameters(player.world.getRegistryManager()).orElseGet(() -> {
					if (player instanceof ClientPlayerEntity clientPlayerEntity) {
						clientPlayerEntity.networkHandler.getConnection().disconnect(Text.translatable("multiplayer.disconnect.invalid_packet"));
					}
					return null;
				})
		);
	}

	public MutableS2CProfileIndependentMessage(PlayerEntity player, boolean isOnClient, Text message, MessageType.Parameters messageType) {
		super(player, isOnClient);
		this.message = message;
		this.messageType = messageType;
	}

	@Override
	public @NotNull EnumSet<QuiltMessageType> getTypes() {
		return EnumSet.of(QuiltMessageType.PROFILE_INDEPENDENT, QuiltMessageType.SERVER, QuiltMessageType.OUTBOUND);
	}

	@Override
	public @NotNull ImmutableS2CProfileIndependentMessage immutableCopy() {
		return new ImmutableS2CProfileIndependentMessage(player, isOnClientSide, message, messageType);
	}

	@Override
	public @NotNull ProfileIndependentMessageS2CPacket asPacket() {
		return new ProfileIndependentMessageS2CPacket(message, messageType.serialize(player.world.getRegistryManager()));
	}

	public Text getMessage() {
		return message;
	}

	public void setMessage(Text message) {
		this.message = message;
	}

	public MessageType.Parameters getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType.Parameters messageType) {
		this.messageType = messageType;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("MutableS2CProfileIndependentMessage{");
		sb.append("message=").append(message);
		sb.append(", messageType=").append(messageType);
		sb.append(", player=").append(player);
		sb.append(", isOnClientSide=").append(isOnClientSide);
		sb.append('}');
		return sb.toString();
	}
}
