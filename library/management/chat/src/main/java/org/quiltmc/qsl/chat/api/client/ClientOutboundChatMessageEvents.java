package org.quiltmc.qsl.chat.api.client;

import org.quiltmc.qsl.base.api.event.Event;

public class ClientOutboundChatMessageEvents {
	public static Event<ModifyChatMessageCallback> MODIFY = Event.create(ModifyChatMessageCallback.class, callbacks -> (message) -> {
		String result = message;
		for (var callback : callbacks) {
			result = callback.beforeChatMessageSent(result);
		}

		return result;
	});

	public static Event<CancelChatMessageCallback> CANCEL = Event.create(CancelChatMessageCallback.class, callbacks -> (message) -> {
		for (var callback : callbacks) {
			if (callback.cancelChatMessage(message)) {
				return true;
			}
		}

		return false;
	});

	@FunctionalInterface
	public interface ModifyChatMessageCallback {
		String beforeChatMessageSent(String message);
	}

	@FunctionalInterface
	public interface CancelChatMessageCallback {
		boolean cancelChatMessage(String message);
	}
}
