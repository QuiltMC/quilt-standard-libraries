package org.quiltmc.qsl.chat.api.client;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.chat.impl.common.ProfileIndependentMessageWrapper;

public class ClientInboundProfileIndependentMessageEvents {
	public static Event<ModifyChatMessageCallback> MODIFY = Event.create(ModifyChatMessageCallback.class, callbacks -> (wrapper) -> {
		for (var callback : callbacks) {
			callback.onReceivedProfileIndependentMessage(wrapper);
		}
	});

	public static Event<CancelChatMessageCallback> CANCEL = Event.create(CancelChatMessageCallback.class, callbacks -> (wrapper) -> {
		for (var callback : callbacks) {
			if (callback.cancelProfileIndependentMessage(wrapper)) {
				return true;
			}
		}

		return false;
	});

	@FunctionalInterface
	public interface ModifyChatMessageCallback {
		void onReceivedProfileIndependentMessage(ProfileIndependentMessageWrapper wrapper);
	}

	@FunctionalInterface
	public interface CancelChatMessageCallback {
		boolean cancelProfileIndependentMessage(ProfileIndependentMessageWrapper wrapper);
	}
}
