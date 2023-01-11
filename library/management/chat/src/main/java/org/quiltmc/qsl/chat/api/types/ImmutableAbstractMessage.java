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
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.chat.api.QuiltMessageType;

import java.util.EnumSet;

/**
 * @param <T> The type of the immutable type
 * @param <S> The type of the packet form of this class
 */
public abstract sealed class ImmutableAbstractMessage<T extends ImmutableAbstractMessage<T, S>, S>
		permits ImmutableC2SChatMessage, ImmutableS2CChatMessage, ImmutableS2CSystemMessage, MutableAbstractMessage {
	protected final @NotNull PlayerEntity player;
	protected final boolean isOnClientSide;

	protected ImmutableAbstractMessage(@NotNull PlayerEntity player, boolean isOnClientSide) {
		this.player = player;
		this.isOnClientSide = isOnClientSide;
	}

	// These are mostly annotated as @NotNull for dev productivity, I get nice warnings about
	// What parts of the subclasses I need to implement still
	public abstract @NotNull EnumSet<QuiltMessageType> getTypes();

	public abstract @NotNull T immutableCopy();

	public abstract @NotNull S asPacket();

	/**
	 * Returns the player associated with this packet, which changes meaning based on the sidedness and direction.
	 * <p>
	 * For System and Profile Independent messages, the player is the target of the message as players cannot send these to the server.
	 * <p>
	 * For Chat messages, on the server inbound, it is the sender of the chat message, while outbound it is the player the message will be sent to.
	 * Chat messages on the client return the client player.
	 * @return the {@link PlayerEntity} associated with this packet
	 */
	public final @NotNull PlayerEntity getPlayer() {
		return player;
	}
}
