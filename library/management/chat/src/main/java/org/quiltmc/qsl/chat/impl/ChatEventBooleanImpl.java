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

import net.minecraft.util.Identifier;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.chat.api.ChatEvent;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.AbstractChatMessage;

import java.util.EnumSet;
import java.util.function.BiFunction;

public class ChatEventBooleanImpl<H> implements ChatEvent<H, Boolean> {
	private final BiFunction<H, EnumSet<QuiltMessageType>, TypedChatApiHook<Boolean>> converter;
	private final Event<TypedChatApiHook<Boolean>> backingEvent = Event.create(TypedChatApiHook.class, hooks -> new TypedChatApiHook<>() {
		@Override
		public EnumSet<QuiltMessageType> getMessageTypes() {
			return EnumSet.allOf(QuiltMessageType.class);
		}

		@Override
		public Boolean handleMessage(AbstractChatMessage<?> message) {
			for (var hook : hooks) {
				if (shouldPassOnMessageToHook(message.getTypes(), hook.getMessageTypes())) {
					if (hook.handleMessage(message)) {
						return true;
					}
				}
			}
			return false;
		}
	});

	public ChatEventBooleanImpl(BiFunction<H, EnumSet<QuiltMessageType>, TypedChatApiHook<Boolean>> converter) {
		this.converter = converter;
	}

	private boolean shouldPassOnMessageToHook(EnumSet<QuiltMessageType> messageTypes, EnumSet<QuiltMessageType> hookTypes) {
		// For every message type
		for (var messageType : messageTypes) {
			// If the hook isn't looking for it
			if (!hookTypes.contains(messageType)) {
				// If it doesn't match the complex rule
				if (!matchesMetaTypeRule(messageType.metaType, hookTypes)) {
					// Not a match
					return false;
				}
			}
		}

		// All message types are wanted, pass it on
		return true;
	}

	private boolean matchesMetaTypeRule(QuiltMessageType.QuiltMetaMessageType metaType, EnumSet<QuiltMessageType> hookTypes) {
		// For every type the hook is looking for
		for (var hookType : hookTypes) {
			// Check if they have the same meta type
			if (hookType.metaType == metaType) {
				// If so, don't pass it on
				// We eliminated equal type previously, so this is only same-meta-different-type
				return false;
			}
		}

		return true;
	}

	public Boolean invoke(AbstractChatMessage<?> message) {
		return backingEvent.invoker().handleMessage(message);
	}

	@Override
	public Boolean invokeOrElse(AbstractChatMessage<?> message, Boolean ifNull) {
		Boolean result = backingEvent.invoker().handleMessage(message);
		return result != null ? result : ifNull;
	}

	@Override
	public void register(EnumSet<QuiltMessageType> types, H handler) {
		backingEvent.register(converter.apply(handler, types));
	}

	@Override
	public void register(@NotNull Identifier phaseIdentifier, EnumSet<QuiltMessageType> types, H handler) {
		backingEvent.register(converter.apply(handler, types));
	}

	@Override
	public void addPhaseOrdering(@NotNull Identifier firstPhase, @NotNull Identifier secondPhase) {
		backingEvent.addPhaseOrdering(firstPhase, secondPhase);
	}
}
