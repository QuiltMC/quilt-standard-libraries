package org.quiltmc.qsl.chat.api.client;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.chat.impl.client.ChatMessageWrapper;

public class ClientInboundChatMessageEvents {
	public static Event<ModifyChatMessageCallback> MODIFY = Event.create(ModifyChatMessageCallback.class, callbacks -> (wrapper) -> {
		for (var callback : callbacks) {
			callback.onReceivedChatMessage(wrapper);
		}
	});

	public static Event<CancelChatMessageCallback> CANCEL = Event.create(CancelChatMessageCallback.class, callbacks -> (wrapper) -> {
		for (var callback : callbacks) {
			if (callback.cancelChatMessage(wrapper)) {
				return true;
			}
		}

		return false;
	});

	@FunctionalInterface
	public interface ModifyChatMessageCallback {
		void onReceivedChatMessage(ChatMessageWrapper wrapper);
	}

	@FunctionalInterface
	public interface CancelChatMessageCallback {
		boolean cancelChatMessage(ChatMessageWrapper wrapper);
	}
}
