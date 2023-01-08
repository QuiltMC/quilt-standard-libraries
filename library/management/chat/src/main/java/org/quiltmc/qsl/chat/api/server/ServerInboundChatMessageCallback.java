package org.quiltmc.qsl.chat.api.server;

import org.quiltmc.qsl.base.api.event.Event;

@FunctionalInterface
public interface ServerInboundChatMessageCallback {
	Event<ServerInboundChatMessageCallback> EVENT = Event.create(ServerInboundChatMessageCallback.class, callbacks -> () -> {
		for (var callback : callbacks) {
			callback.onMessageReceived();
		}
	});

	void onMessageReceived();
}
