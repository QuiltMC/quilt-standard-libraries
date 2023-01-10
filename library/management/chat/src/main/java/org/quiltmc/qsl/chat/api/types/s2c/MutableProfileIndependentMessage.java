package org.quiltmc.qsl.chat.api.types.s2c;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.s2c.play.ProfileIndependentMessageS2CPacket;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.MutableAbstractMessage;

import java.util.EnumSet;

public class MutableProfileIndependentMessage extends MutableAbstractMessage<ImmutableProfileIndependentMessage, ProfileIndependentMessageS2CPacket> {
	private final PlayerEntity player;
	private Text message;
	private MessageType.Parameters messageType;

	public MutableProfileIndependentMessage(PlayerEntity player, ProfileIndependentMessageS2CPacket packet) {
		this(
				player,
				packet.message(),
				packet.messageType().createParameters(player.world.getRegistryManager()).orElseGet(() -> {
					if (player instanceof ClientPlayerEntity clientPlayerEntity) {
						clientPlayerEntity.networkHandler.getConnection().disconnect(Text.translatable("multiplayer.disconnect.invalid_packet"));
					}
					return null;
				})
		);
	}

	public MutableProfileIndependentMessage(PlayerEntity player, Text message, MessageType.Parameters messageType) {
		this.player = player;
		this.message = message;
		this.messageType = messageType;
	}

	@Override
	public @NotNull EnumSet<QuiltMessageType> getTypes() {
		return EnumSet.of(QuiltMessageType.PROFILE_INDEPENDENT, QuiltMessageType.SERVER, QuiltMessageType.OUTBOUND);
	}

	@Override
	public @NotNull ImmutableProfileIndependentMessage immutableCopy() {
		return new ImmutableProfileIndependentMessage(player, message, messageType);
	}

	@Override
	public @NotNull ProfileIndependentMessageS2CPacket packet() {
		return new ProfileIndependentMessageS2CPacket(message, messageType.serialize(player.world.getRegistryManager()));
	}

	public PlayerEntity getPlayer() {
		return player;
	}

	public Text getMessage() {
		return message;
	}

	public void setMessage(Text message) {
		this.message = message;
	}

	public MessageType.Parameters getMessageType() {
		return messageType;
	}

	public void setMessageType(MessageType.Parameters messageType) {
		this.messageType = messageType;
	}
}
