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

import org.jetbrains.annotations.NotNull;

import org.quiltmc.qsl.chat.api.ChatEvent;
import org.quiltmc.qsl.chat.api.QuiltChatEvents;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.AbstractChatMessage;

/**
 * Converts various callbacks from {@link QuiltChatEvents} into {@link ChatEvent.TypedChatApiHook}s
 */
public class InternalChatEventCallbackConverters {
	private InternalChatEventCallbackConverters() {}

	public static ChatEvent.TypedChatApiHook<AbstractChatMessage<?>> modifyToHook(QuiltChatEvents.Modify modify, EnumSet<QuiltMessageType> types) {
		return new ChatEvent.TypedChatApiHook<>() {
			@Override
			public EnumSet<QuiltMessageType> getMessageTypes() {
				return types;
			}

			@Override
			public AbstractChatMessage<?> handleMessage(@NotNull AbstractChatMessage<?> message) {
				return modify.modifyMessage(message);
			}
		};
	}

	public static ChatEvent.TypedChatApiHook<Boolean> cancelToHook(QuiltChatEvents.Cancel cancel, EnumSet<QuiltMessageType> types) {
		return new ChatEvent.TypedChatApiHook<>() {
			@Override
			public EnumSet<QuiltMessageType> getMessageTypes() {
				return types;
			}

			@Override
			public Boolean handleMessage(@NotNull AbstractChatMessage<?> message) {
				return cancel.shouldCancelMessage(message);
			}
		};
	}

	public static ChatEvent.TypedChatApiHook<Void> cancelledToHook(QuiltChatEvents.Cancelled cancelled, EnumSet<QuiltMessageType> types) {
		return new ChatEvent.TypedChatApiHook<>() {
			@Override
			public EnumSet<QuiltMessageType> getMessageTypes() {
				return types;
			}

			@Override
			public Void handleMessage(@NotNull AbstractChatMessage<?> message) {
				cancelled.onMessageCancelled(message);
				return null;
			}
		};
	}

	public static ChatEvent.TypedChatApiHook<Void> beforeToHook(QuiltChatEvents.Before before, EnumSet<QuiltMessageType> types) {
		return new ChatEvent.TypedChatApiHook<>() {
			@Override
			public EnumSet<QuiltMessageType> getMessageTypes() {
				return types;
			}

			@Override
			public Void handleMessage(@NotNull AbstractChatMessage<?> message) {
				before.beforeMessage(message);
				return null;
			}
		};
	}

	public static ChatEvent.TypedChatApiHook<Void> afterToHook(QuiltChatEvents.After after, EnumSet<QuiltMessageType> types) {
		return new ChatEvent.TypedChatApiHook<>() {
			@Override
			public EnumSet<QuiltMessageType> getMessageTypes() {
				return types;
			}

			@Override
			public Void handleMessage(@NotNull AbstractChatMessage<?> message) {
				after.afterMessage(message);
				return null;
			}
		};
	}
}
