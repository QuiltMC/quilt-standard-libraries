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
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.ImmutableAbstractMessage;

import java.util.EnumSet;
import java.util.function.Consumer;

// Cant extend event because the constructor is private, gotta reproduce some of the API surface
public class ChatVoidEvent {
	private final Event<ChatApiHook> backingEvent = Event.create(ChatApiHook.class, hooks -> new ChatApiHook() {
		@Override
		public EnumSet<QuiltMessageType> getMessageTypes() {
			return EnumSet.allOf(QuiltMessageType.class);
		}

		@Override
		public void handleMessage(ImmutableAbstractMessage<?, ?> message) {
			for (var hook : hooks) {
				if (shouldPassOnMessageToHook(message.getTypes(), hook.getMessageTypes())) {
					hook.handleMessage(message);
				}
			}
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
	public void invoke(ImmutableAbstractMessage<?, ?> message) {
		backingEvent.invoker().handleMessage(message);
	}

	public void register(EnumSet<QuiltMessageType> types, Consumer<ImmutableAbstractMessage<?, ?>> handler) {
		backingEvent.register(new ChatApiHook() {
			@Override
			public EnumSet<QuiltMessageType> getMessageTypes() {
				return types;
			}

			@Override
			public void handleMessage(ImmutableAbstractMessage<?, ?> message) {
				handler.accept(message);
			}
		});
	}

	public void register(@NotNull Identifier phaseIdentifier, EnumSet<QuiltMessageType> types, Consumer<ImmutableAbstractMessage<?, ?>> handler) {
		backingEvent.register(phaseIdentifier, new ChatApiHook() {
			@Override
			public EnumSet<QuiltMessageType> getMessageTypes() {
				return types;
			}

			@Override
			public void handleMessage(ImmutableAbstractMessage<?, ?> message) {
				handler.accept(message);
			}
		});
	}

	public void addPhaseOrdering(@NotNull Identifier firstPhase, @NotNull Identifier secondPhase) {
		backingEvent.addPhaseOrdering(firstPhase, secondPhase);
	}

	private interface ChatApiHook {
		EnumSet<QuiltMessageType> getMessageTypes();
		void handleMessage(ImmutableAbstractMessage<?, ?> message);
	}
}
