package org.quiltmc.qsl.chat.api.client;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.chat.impl.client.SystemMessageWrapper;

public class ClientInboundSystemMessageEvents {
	public static Event<ModifyChatMessageCallback> MODIFY = Event.create(ModifyChatMessageCallback.class, callbacks -> (wrapper) -> {
		for (var callback : callbacks) {
			callback.modifyReceivedSystemMessage(wrapper);
		}
	});

	public static Event<CancelChatMessageCallback> CANCEL = Event.create(CancelChatMessageCallback.class, callbacks -> (wrapper) -> {
		for (var callback : callbacks) {
			if (callback.cancelSystemMessage(wrapper)) {
				return true;
			}
		}

		return false;
	});

	@FunctionalInterface
	public interface ModifyChatMessageCallback {
		void modifyReceivedSystemMessage(SystemMessageWrapper wrapper);
	}

	@FunctionalInterface
	public interface CancelChatMessageCallback {
		boolean cancelSystemMessage(SystemMessageWrapper wrapper);
	}
}
