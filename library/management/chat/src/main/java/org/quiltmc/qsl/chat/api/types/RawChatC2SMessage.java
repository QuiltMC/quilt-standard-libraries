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

import net.minecraft.entity.player.PlayerEntity;

import org.quiltmc.qsl.chat.api.QuiltMessageType;

/**
 * A raw C2S chat message. This is a message before it has been signed by the client.
 *
 * @see ChatC2SMessage
 */
public class RawChatC2SMessage extends AbstractChatMessage<String> {
	private final String message;

	public RawChatC2SMessage(@NotNull PlayerEntity player, boolean isClient, String message) {
		super(player, isClient);
		this.message = message;
	}

	@Override
	public @NotNull EnumSet<QuiltMessageType> getTypes() {
		return EnumSet.of(QuiltMessageType.CHAT, QuiltMessageType.CLIENT, QuiltMessageType.OUTBOUND);
	}

	@Contract(pure = true)
	public String getMessage() {
		return this.message;
	}

	@Contract(value = "_ -> new", pure = true)
	public RawChatC2SMessage withMessage(String message) {
		return new RawChatC2SMessage(this.player, this.isClient, message);
	}

	@Override
	public @NotNull String serialized() {
		return this.message;
	}

	@Override
	public String toString() {
		return "RawChatC2SMessage{" + "message='" + this.message + '\'' + '}';
	}
}
