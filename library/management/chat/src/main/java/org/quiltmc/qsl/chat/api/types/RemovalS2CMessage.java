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

import java.util.EnumSet;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.impl.InternalMessageTypesFactory;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.MessageSignature;
import net.minecraft.network.packet.s2c.play.MessageRemovalS2CPacket;

public class RemovalS2CMessage extends AbstractChatMessage<MessageRemovalS2CPacket> {
	private final MessageSignature.Indexed signature;

	public RemovalS2CMessage(PlayerEntity player, boolean isClient, MessageRemovalS2CPacket packet) {
		this(player, isClient, packet.signature());
	}

	public RemovalS2CMessage(PlayerEntity player, boolean isClient, MessageSignature.Indexed signature) {
		super(player, isClient);
		this.signature = signature;
	}

	@Override
	public @NotNull EnumSet<QuiltMessageType> getTypes() {
		return InternalMessageTypesFactory.s2cType(QuiltMessageType.REMOVAL, isClient);
	}

	public MessageSignature.Indexed getSignature() {
		return signature;
	}

	@Contract(value = "_ -> new", pure = true)
	public RemovalS2CMessage withSignature(MessageSignature.Indexed signature) {
		return new RemovalS2CMessage(this.player, this.isClient, signature);
	}

	@Override
	public @NotNull MessageRemovalS2CPacket serialized() {
		return new MessageRemovalS2CPacket(signature);
	}
}
