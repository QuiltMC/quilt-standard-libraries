package org.quiltmc.qsl.chat.api.server;

import org.quiltmc.qsl.base.api.event.Event;

@FunctionalInterface
public interface ServerOutboundSystemMessageCallback {
	Event<ServerOutboundSystemMessageCallback> EVENT = Event.create(ServerOutboundSystemMessageCallback.class, callbacks -> () -> {
		for (var callback : callbacks) {
			callback.beforeMessageSent();
		}
	});

	void beforeMessageSent();
}
