package org.quiltmc.qsl.chat.impl.server;

import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.OutgoingMessage;
import net.minecraft.server.network.ServerPlayerEntity;

public class SendMessageWrapper {
	private final ServerPlayerEntity target;
	private OutgoingMessage message;
	private boolean filterMaskEnabled;
	private MessageType.Parameters parameters;

	public SendMessageWrapper(ServerPlayerEntity target, OutgoingMessage message, boolean filterMaskEnabled, MessageType.Parameters parameters) {
		this.target = target;
		this.message = message;
		this.filterMaskEnabled = filterMaskEnabled;
		this.parameters = parameters;
	}


	public ServerPlayerEntity getTarget() {
		return target;
	}

	public OutgoingMessage getMessage() {
		return message;
	}

	public void setMessage(OutgoingMessage message) {
		this.message = message;
	}

	public boolean isFilterMaskEnabled() {
		return filterMaskEnabled;
	}

	public void setFilterMaskEnabled(boolean filterMaskEnabled) {
		this.filterMaskEnabled = filterMaskEnabled;
	}

	public MessageType.Parameters getParameters() {
		return parameters;
	}

	public void setParameters(MessageType.Parameters parameters) {
		this.parameters = parameters;
	}
}
