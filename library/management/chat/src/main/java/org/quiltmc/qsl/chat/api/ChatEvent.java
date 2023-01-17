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

package org.quiltmc.qsl.chat.api;

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.chat.api.types.AbstractChatMessage;

import java.util.EnumSet;

public interface ChatEvent<H, R> {
	/**
	 * {@return the result of invoking this event, or null if there are no listeners}
	 */
	@Nullable R invoke(AbstractChatMessage<?> message);

	/**
	 * {@return the result of invoking this event, or ifNull if the result is null}
	 */
	R invokeOrElse(AbstractChatMessage<?> message, R ifNull);

	void register(EnumSet<QuiltMessageType> types, H handler);

	void register(@NotNull Identifier phaseIdentifier, EnumSet<QuiltMessageType> types, H handler);

	void addPhaseOrdering(@NotNull Identifier firstPhase, @NotNull Identifier secondPhase);

	interface TypedChatApiHook<R> {
		EnumSet<QuiltMessageType> getMessageTypes();
		R handleMessage(AbstractChatMessage<?> message);
	}
}
