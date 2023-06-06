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

import org.quiltmc.qsl.chat.api.QuiltChatEvents;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.AbstractChatMessage;

/**
 * Converts various callbacks from {@link QuiltChatEvents} into {@link TypedChatApiHook}s.
 */
public class InternalChatEventCallbackConverters {
	private InternalChatEventCallbackConverters() { }

	public static TypedChatApiHook<AbstractChatMessage<?>> modifyToHook(QuiltChatEvents.Modify modify, EnumSet<QuiltMessageType> types) {
		return new TypedChatApiHook<>() {
			@Override
			public EnumSet<QuiltMessageType> getMessageTypes() {
				return types;
			}

			@Override
			public AbstractChatMessage<?> handleMessage(@NotNull AbstractChatMessage<?> message) {
				return modify.modifyMessage(message);
			}

			@Override
			public String getOriginName() {
				return modify.getClass().getName();
			}
		};
	}

	public static TypedChatApiHook<Boolean> cancelToHook(QuiltChatEvents.Cancel cancel, EnumSet<QuiltMessageType> types) {
		return new TypedChatApiHook<>() {
			@Override
			public EnumSet<QuiltMessageType> getMessageTypes() {
				return types;
			}

			@Override
			public Boolean handleMessage(@NotNull AbstractChatMessage<?> message) {
				return cancel.shouldCancelMessage(message);
			}

			@Override
			public String getOriginName() {
				return cancel.getClass().getName();
			}
		};
	}

	public static TypedChatApiHook<Void> cancelledToHook(QuiltChatEvents.Cancelled cancelled, EnumSet<QuiltMessageType> types) {
		return new TypedChatApiHook<>() {
			@Override
			public EnumSet<QuiltMessageType> getMessageTypes() {
				return types;
			}

			@Override
			public Void handleMessage(@NotNull AbstractChatMessage<?> message) {
				cancelled.onMessageCancelled(message);
				return null;
			}

			@Override
			public String getOriginName() {
				return cancelled.getClass().getName();
			}
		};
	}

	public static TypedChatApiHook<Void> beforeToHook(QuiltChatEvents.Before before, EnumSet<QuiltMessageType> types) {
		return new TypedChatApiHook<>() {
			@Override
			public EnumSet<QuiltMessageType> getMessageTypes() {
				return types;
			}

			@Override
			public Void handleMessage(@NotNull AbstractChatMessage<?> message) {
				before.beforeMessage(message);
				return null;
			}

			@Override
			public String getOriginName() {
				return before.getClass().getName();
			}
		};
	}

	public static TypedChatApiHook<Void> afterToHook(QuiltChatEvents.After after, EnumSet<QuiltMessageType> types) {
		return new TypedChatApiHook<>() {
			@Override
			public EnumSet<QuiltMessageType> getMessageTypes() {
				return types;
			}

			@Override
			public Void handleMessage(@NotNull AbstractChatMessage<?> message) {
				after.afterMessage(message);
				return null;
			}

			@Override
			public String getOriginName() {
				return after.getClass().getName();
			}
		};
	}
}
