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
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.chat.api.types.AbstractChatMessage;

import java.util.EnumSet;
import java.util.function.BiFunction;

// Cant extend event because the constructor is private, gotta reproduce some of the API surface
public class ChatEvent<H, R> {
	private final boolean shouldPreformAssignableCheck;
	private final BiFunction<H, EnumSet<QuiltMessageType>, TypedChatApiHook<R>> converter;

	public ChatEvent(boolean shouldPreformAssignableCheck, BiFunction<H, EnumSet<QuiltMessageType>, TypedChatApiHook<R>> converter) {
		this.shouldPreformAssignableCheck = shouldPreformAssignableCheck;
		this.converter = converter;
	}

	private final Event<TypedChatApiHook<@Nullable R>> backingEvent = Event.create(TypedChatApiHook.class, hooks -> new TypedChatApiHook<>() {
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
					if (shouldPreformAssignableCheck) {
						if (tmpResult == null) {
							throw new NullPointerException("Handler attached to a ChatEvent returned a null result!");
						} else if (!message.getClass().isAssignableFrom(tmpResult.getClass())) {
							throw new IllegalArgumentException(
									"Handler attached to a ChatEvent returned a non-similar value! " +
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

	public void register(EnumSet<QuiltMessageType> types, H handler) {
		backingEvent.register(converter.apply(handler, types));
	}

	public void register(@NotNull Identifier phaseIdentifier, EnumSet<QuiltMessageType> types, H handler) {
		backingEvent.register(phaseIdentifier, converter.apply(handler, types));
	}

	public void addPhaseOrdering(@NotNull Identifier firstPhase, @NotNull Identifier secondPhase) {
		backingEvent.addPhaseOrdering(firstPhase, secondPhase);
	}

	public interface TypedChatApiHook<R> {
		EnumSet<QuiltMessageType> getMessageTypes();
		R handleMessage(AbstractChatMessage<?> message);
	}
}
