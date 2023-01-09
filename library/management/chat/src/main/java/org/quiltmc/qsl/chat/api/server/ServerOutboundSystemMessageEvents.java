package org.quiltmc.qsl.chat.api.server;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.chat.impl.server.SystemMessageWrapper;

public class ServerOutboundSystemMessageEvents {
	public static Event<ModifyChatMessageCallback> MODIFY = Event.create(ModifyChatMessageCallback.class, callbacks -> (wrapper) -> {
		for (var callback : callbacks) {
			callback.beforeSystemMessageSent(wrapper);
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
		void beforeSystemMessageSent(SystemMessageWrapper message);
	}

	@FunctionalInterface
	public interface CancelChatMessageCallback {
		boolean cancelSystemMessage(SystemMessageWrapper wrapper);
	}
}
