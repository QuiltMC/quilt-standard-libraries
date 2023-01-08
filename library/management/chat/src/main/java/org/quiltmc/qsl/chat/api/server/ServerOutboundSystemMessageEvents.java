package org.quiltmc.qsl.chat.api.server;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.quiltmc.qsl.base.api.event.Event;

public class ServerOutboundSystemMessageEvents {
	public static Event<ModifyChatMessageCallback> MODIFY = Event.create(ModifyChatMessageCallback.class, callbacks -> (message) -> {
		Text result = message;
		for (var callback : callbacks) {
			result = callback.beforeChatMessageSent(result);
		}

		return result;
	});

	public static Event<CancelChatMessageCallback> CANCEL = Event.create(CancelChatMessageCallback.class, callbacks -> (target, message) -> {
		for (var callback : callbacks) {
			if (callback.cancelChatMessage(target, message)) {
				return true;
			}
		}

		return false;
	});

	@FunctionalInterface
	public interface ModifyChatMessageCallback {
		Text beforeChatMessageSent(Text message);
	}

	@FunctionalInterface
	public interface CancelChatMessageCallback {
		boolean cancelChatMessage(ServerPlayerEntity target, Text message);
	}
}
