package org.quiltmc.qsl.chat.impl.common;

import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.s2c.play.ProfileIndependentMessageS2CPacket;
import net.minecraft.text.Text;
import org.quiltmc.loader.api.minecraft.ClientOnly;

public class ProfileIndependentMessageWrapper {
	private Text message;
	private MessageType.Serialized messageType;

	@ClientOnly
	public ProfileIndependentMessageWrapper(ProfileIndependentMessageS2CPacket packet) {
		this(
				packet.message(),
				packet.messageType()
		);
	}

	public ProfileIndependentMessageWrapper(Text message, MessageType.Serialized messageType) {
		this.message = message;
		this.messageType = messageType;
	}

	public ProfileIndependentMessageS2CPacket asPacket() {
		return new ProfileIndependentMessageS2CPacket(message, messageType);
	}

	public Text getMessage() {
		return message;
	}

	public void setMessage(Text message) {
		this.message = message;
	}

	public MessageType.Serialized getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType.Serialized messageType) {
		this.messageType = messageType;
	}
}
