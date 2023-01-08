package org.quiltmc.qsl.chat.api.server;

import org.quiltmc.qsl.base.api.event.Event;

@FunctionalInterface
public interface ServerOutboundChatMessageCallback {
	Event<ServerOutboundChatMessageCallback> EVENT = Event.create(ServerOutboundChatMessageCallback.class, callbacks -> () -> {
		for (var callback : callbacks) {
			callback.beforeMessageSent();
		}
	});

	void beforeMessageSent();
}
