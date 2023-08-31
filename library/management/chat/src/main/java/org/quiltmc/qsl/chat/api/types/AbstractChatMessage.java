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
 * An abstract message, extended for the various types of messages.
 *
 * @param <S> The type of the serialized form of this message, usually a {@link net.minecraft.network.packet.Packet} but not required.
 */
public abstract class AbstractChatMessage<S> {
	protected final @NotNull PlayerEntity player;
	protected final boolean isClient;

	protected AbstractChatMessage(@NotNull PlayerEntity player, boolean isClient) {
		this.player = player;
		this.isClient = isClient;
	}

	// These are mostly annotated as @NotNull for dev productivity, I get nice warnings about
	// What parts of the subclasses I need to implement still

	/**
	 * {@return the set of {@link QuiltMessageType}s associated with this message}
	 */
	@Contract(pure = true)
	public abstract @NotNull EnumSet<QuiltMessageType> getTypes();

	/**
	 * {@return this message in its serialized form}
	 */
	@Contract(value = " -> new", pure = true)
	public abstract @NotNull S serialized();

	/**
	 * Returns the player associated with this packet, which changes meaning based on the sidedness and direction.
	 * <p>
	 * For System and Profile Independent messages, the player is the target of the message as players cannot send these to the server.
	 * <p>
	 * For Chat messages, on the server inbound, it is the sender of the chat message, while outbound it is the player the message will be sent to.
	 * Chat messages on the client return the client player.
	 *
	 * {@return the {@link PlayerEntity} associated with this packet}
	 */
	@Contract(pure = true)
	public final @NotNull PlayerEntity getPlayer() {
		return this.player;
	}

	/**
	 * {@return if this message is on the client side or not}
	 * <p>
	 * This can also be determined through checking for {@link QuiltMessageType#CLIENT}.
	 */
	@Contract(pure = true)
	public boolean isClient() {
		return this.isClient;
	}
}
