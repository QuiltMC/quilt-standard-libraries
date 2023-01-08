package org.quiltmc.qsl.chat.api.server;

import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.OutgoingMessage;
import net.minecraft.server.network.ServerPlayerEntity;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.chat.impl.server.SendMessageWrapper;

public class ServerOutboundChatMessageEvents {
	public static Event<ModifyChatMessageCallback> MODIFY = Event.create(ModifyChatMessageCallback.class, callbacks -> (wrapper) -> {
		for (var callback : callbacks) {
			callback.beforeChatMessageSent(wrapper);
		}
	});

	public static Event<CancelChatMessageCallback> CANCEL = Event.create(CancelChatMessageCallback.class, callbacks -> (target, message, filterMaskEnabled, parameters) -> {
		for (var callback : callbacks) {
			if (callback.cancelChatMessage(target, message, filterMaskEnabled, parameters)) {
				return true;
			}
		}

		return false;
	});

	@FunctionalInterface
	public interface ModifyChatMessageCallback {
		void beforeChatMessageSent(SendMessageWrapper wrapper);
	}

	@FunctionalInterface
	public interface CancelChatMessageCallback {
		boolean cancelChatMessage(ServerPlayerEntity target, OutgoingMessage message, boolean filterMaskEnabled, MessageType.Parameters parameters);
	}
}
