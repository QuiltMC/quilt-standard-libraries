package org.quiltmc.qsl.chat.impl;

import org.quiltmc.qsl.chat.api.QuiltChatEvents;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.AbstractChatMessage;

import java.util.EnumSet;

public class InternalChatEventHandlerConverters {
	private InternalChatEventHandlerConverters() { }

	public static ChatEventImpl.TypedChatApiHook<AbstractChatMessage<?>> modifyToHook(QuiltChatEvents.Modify modify, EnumSet<QuiltMessageType> types) {
		return new ChatEventImpl.TypedChatApiHook<>() {
			@Override
			public EnumSet<QuiltMessageType> getMessageTypes() {
				return types;
			}

			@Override
			public AbstractChatMessage<?> handleMessage(AbstractChatMessage<?> message) {
				return modify.handleMessage(message);
			}
		};
	}

	public static ChatEventImpl.TypedChatApiHook<Boolean> cancelToHook(QuiltChatEvents.Cancel cancel, EnumSet<QuiltMessageType> types) {
		return new ChatEventImpl.TypedChatApiHook<>() {
			@Override
			public EnumSet<QuiltMessageType> getMessageTypes() {
				return types;
			}

			@Override
			public Boolean handleMessage(AbstractChatMessage<?> message) {
				return cancel.shouldCancel(message);
			}
		};
	}

	public static ChatEventImpl.TypedChatApiHook<Void> listenToHook(QuiltChatEvents.Listen listen, EnumSet<QuiltMessageType> types) {
		return new ChatEventImpl.TypedChatApiHook<>() {
			@Override
			public EnumSet<QuiltMessageType> getMessageTypes() {
				return types;
			}

			@Override
			public Void handleMessage(AbstractChatMessage<?> message) {
				listen.handleMessage(message);
				return null;
			}
		};
	}
}
