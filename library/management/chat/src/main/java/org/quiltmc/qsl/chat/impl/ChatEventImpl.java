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
import org.jetbrains.annotations.Nullable;

import net.minecraft.util.Identifier;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.chat.api.ChatEvent;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.AbstractChatMessage;

/**
 * The common implementation of {@link ChatEvent}. If this event is set to preform assignable checks, then it will require that any return values are both
 * non-null and assignable to the original class passed to {@link ChatEvent#invoke(AbstractChatMessage)}, throwing if not.
 */
public class ChatEventImpl<C, R> implements ChatEvent<C, R> {
	private final boolean shouldPreformAssignableCheck;
	private final BiFunction<C, EnumSet<QuiltMessageType>, TypedChatApiHook<R>> converter;

	public ChatEventImpl(boolean shouldPreformAssignableCheck, BiFunction<C, EnumSet<QuiltMessageType>, TypedChatApiHook<R>> converter) {
		this.shouldPreformAssignableCheck = shouldPreformAssignableCheck;
		this.converter = converter;
	}

	private final Event<TypedChatApiHook<@Nullable R>> backingEvent = Event.create(TypedChatApiHook.class, hooks -> new TypedChatApiHook<>() {
		@Override
		public EnumSet<QuiltMessageType> getMessageTypes() {
			return EnumSet.allOf(QuiltMessageType.class);
		}

		@Override
		public R handleMessage(@NotNull AbstractChatMessage<?> message) {
			R result = null;

			for (var hook : hooks) {
				if (ChatEventImpl.this.shouldPassOnMessageToHook(message.getTypes(), hook.getMessageTypes())) {
					R tmpResult = hook.handleMessage(message);
					if (ChatEventImpl.this.shouldPreformAssignableCheck) {
						if (tmpResult == null) {
							throw new NullPointerException("Callback attached to a ChatEvent returned a null result!");
						} else if (!message.getClass().isAssignableFrom(tmpResult.getClass())) {
							throw new IllegalArgumentException(
									"Callback attached to a ChatEvent returned a non-similar value! " +
											"Expected a subclass or instance of " + message.getClass().getName() + " but got a " + tmpResult.getClass().getName() + "!"
							);
						}
					}

					result = tmpResult;
				}
			}

			return result;
		}
	});

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

	@Override
	public @Nullable R invoke(@NotNull AbstractChatMessage<?> message) {
		return this.backingEvent.invoker().handleMessage(message);
	}

	@Override
	public R invokeOrElse(@NotNull AbstractChatMessage<?> message, @NotNull R ifNull) {
		R result = this.backingEvent.invoker().handleMessage(message);
		return result != null ? result : ifNull;
	}

	@Override
	public void register(@NotNull EnumSet<QuiltMessageType> types, @NotNull C callback) {
		this.backingEvent.register(this.converter.apply(callback, types));
	}

	@Override
	public void register(@NotNull Identifier phaseIdentifier, @NotNull EnumSet<QuiltMessageType> types, @NotNull C callback) {
		this.backingEvent.register(phaseIdentifier, this.converter.apply(callback, types));
	}

	@Override
	public void addPhaseOrdering(@NotNull Identifier firstPhase, @NotNull Identifier secondPhase) {
		this.backingEvent.addPhaseOrdering(firstPhase, secondPhase);
	}
}
