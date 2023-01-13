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
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.AbstractChatMessage;

import java.util.EnumSet;
import java.util.function.Function;

// Cant extend event because the constructor is private, gotta reproduce some of the API surface
public class ChatEvent<R> {
	private final boolean shouldPreformAssignableCheck;

	public ChatEvent(boolean shouldPreformAssignableCheck) {
		this.shouldPreformAssignableCheck = shouldPreformAssignableCheck;
	}

	private final Event<ChatApiHook<@Nullable R>> backingEvent = Event.create(ChatApiHook.class, hooks -> new ChatApiHook<>() {
		@Override
		public EnumSet<QuiltMessageType> getMessageTypes() {
			return EnumSet.allOf(QuiltMessageType.class);
		}

		@Override
		public R handleMessage(AbstractChatMessage<?> message) {
			R result = null;

			for (var hook : hooks) {
				if (shouldPassOnMessageToHook(message.getTypes(), hook.getMessageTypes())) {
					R tmpResult = hook.handleMessage(message);
					if (tmpResult != null) {
						if (shouldPreformAssignableCheck && !message.getClass().isAssignableFrom(tmpResult.getClass())) {
							throw new IllegalArgumentException(
									"Handler attached to a ChatEvent returned a non-similar value! " +
									"Expected a subclass or instance of " + message.getClass().getName() + " but got a " + tmpResult.getClass().getName() + "!"
							);
						} else {
							result = tmpResult;
						}
					} else {
						throw new NullPointerException("Handler attached to a ChatEvent returned a null result!");
					}
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

	/**
	 * @return The result of invoking this event, or null if there are no listeners
	 */
	public @Nullable R invoke(AbstractChatMessage<?> message) {
		return backingEvent.invoker().handleMessage(message);
	}

	/**
	 * @return The result of invoking this event, or ifNull if there are no listeners
	 */
	public R invoke(AbstractChatMessage<?> message, R ifNull) {
		R result = backingEvent.invoker().handleMessage(message);
		if (result == null) {
			return ifNull;
		} else {
			return result;
		}
	}

	public void register(EnumSet<QuiltMessageType> types, Function<AbstractChatMessage<?>, R> handler) {
		backingEvent.register(new ChatApiHook<>() {
			@Override
			public EnumSet<QuiltMessageType> getMessageTypes() {
				return types;
			}

			@Override
			public R handleMessage(AbstractChatMessage<?> message) {
				return handler.apply(message);
			}
		});
	}

	public void register(@NotNull Identifier phaseIdentifier, EnumSet<QuiltMessageType> types, Function<AbstractChatMessage<?>, @NotNull R> handler) {
		backingEvent.register(phaseIdentifier, new ChatApiHook<>() {
			@Override
			public EnumSet<QuiltMessageType> getMessageTypes() {
				return types;
			}

			@Override
			public R handleMessage(AbstractChatMessage<?> message) {
				return handler.apply(message);
			}
		});
	}

	public void addPhaseOrdering(@NotNull Identifier firstPhase, @NotNull Identifier secondPhase) {
		backingEvent.addPhaseOrdering(firstPhase, secondPhase);
	}

	private interface ChatApiHook<R> {
		EnumSet<QuiltMessageType> getMessageTypes();
		R handleMessage(AbstractChatMessage<?> message);
	}
}
