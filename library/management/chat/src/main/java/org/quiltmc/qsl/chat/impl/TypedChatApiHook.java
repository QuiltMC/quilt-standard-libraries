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

package org.quiltmc.qsl.chat.impl;

import java.util.EnumSet;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.AbstractChatMessage;

/**
 * The common interface describing a chat message hook. This should almost never be implemented manually, instead
 * relying on various converters for each event to convert a {@link FunctionalInterface} into one of these.
 *
 * @param <R> the return type of handling a message with this hook
 */
public interface TypedChatApiHook<R> {
	/**
	 * @return The message types this hook is expecting
	 */
	EnumSet<QuiltMessageType> getMessageTypes();

	/**
	 * @param message The message to process
	 * @return The result of processing this message
	 */
	R handleMessage(@NotNull AbstractChatMessage<?> message);

	/**
	 * Used for debugging. This method should return the "origin name" of this hook, or null if such a construct does not exist.
	 *
	 * @return The "origin name" of this hook
	 */
	String getOriginName();
}
