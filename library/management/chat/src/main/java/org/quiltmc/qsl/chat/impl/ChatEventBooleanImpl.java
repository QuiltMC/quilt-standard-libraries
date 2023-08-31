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

package org.quiltmc.qsl.chat.impl;

import java.util.EnumSet;
import java.util.function.BiFunction;

import org.jetbrains.annotations.NotNull;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.chat.api.ChatEvent;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.AbstractChatMessage;

/**
 * An implementation of {@link ChatEvent} that always returns a {@link Boolean} and has special short circuiting logic for if a callback returns true.
 * This implementation is intended be used for any cancellation events.
 */
public class ChatEventBooleanImpl<C> implements ChatEvent<C, Boolean> {
	private final BiFunction<C, EnumSet<QuiltMessageType>, TypedChatApiHook<Boolean>> converter;
	private final Event<TypedChatApiHook<Boolean>> backingEvent = Event.create(TypedChatApiHook.class, hooks -> new TypedChatApiHook<>() {
		@Override
		public EnumSet<QuiltMessageType> getMessageTypes() {
			return EnumSet.allOf(QuiltMessageType.class);
		}

		@Override
		public Boolean handleMessage(@NotNull AbstractChatMessage<?> message) {
			for (var hook : hooks) {
				if (ChatEventBooleanImpl.this.shouldPassOnMessageToHook(message.getTypes(), hook.getMessageTypes())) {
					if (hook.handleMessage(message)) {
						return true;
					}
				}
			}

			return false;
		}
	});

	public ChatEventBooleanImpl(BiFunction<C, EnumSet<QuiltMessageType>, TypedChatApiHook<Boolean>> converter) {
		this.converter = converter;
	}

	private boolean shouldPassOnMessageToHook(EnumSet<QuiltMessageType> messageTypes, EnumSet<QuiltMessageType> hookTypes) {
		// For every message type
		for (var messageType : messageTypes) {
			// If the hook isn't looking for it
			if (!hookTypes.contains(messageType)) {
				// If it doesn't match the complex rule
				if (!this.matchesMetaTypeRule(messageType.metaType, hookTypes)) {
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

	public Boolean invoke(@NotNull AbstractChatMessage<?> message) {
		return this.backingEvent.invoker().handleMessage(message);
	}

	@Override
	public Boolean invokeOrElse(@NotNull AbstractChatMessage<?> message, @NotNull Boolean ifNull) {
		Boolean result = this.backingEvent.invoker().handleMessage(message);
		return result != null ? result : ifNull;
	}

	@Override
	public void register(@NotNull EnumSet<QuiltMessageType> types, @NotNull C callback) {
		this.backingEvent.register(this.converter.apply(callback, types));
	}

	@Override
	public void register(@NotNull Identifier phaseIdentifier, @NotNull EnumSet<QuiltMessageType> types, @NotNull C callback) {
		this.backingEvent.register(this.converter.apply(callback, types));
	}

	@Override
	public void addPhaseOrdering(@NotNull Identifier firstPhase, @NotNull Identifier secondPhase) {
		this.backingEvent.addPhaseOrdering(firstPhase, secondPhase);
	}
}
