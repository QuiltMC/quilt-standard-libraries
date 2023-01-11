package org.quiltmc.qsl.chat.api.types;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.s2c.play.ProfileIndependentMessageS2CPacket;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.chat.api.QuiltMessageType;

import java.util.EnumSet;

public final class ImmutableS2CProfileIndependentMessage extends MutableAbstractMessage<ImmutableS2CProfileIndependentMessage, ProfileIndependentMessageS2CPacket> {
	private final Text message;
	private final MessageType.Parameters messageType;

	public ImmutableS2CProfileIndependentMessage(PlayerEntity player, boolean isOnClientSide, ProfileIndependentMessageS2CPacket packet) {
		this(
				player,
				isOnClientSide,
				packet.message(),
				packet.messageType().createParameters(player.world.getRegistryManager()).orElseGet(() -> {
					if (player instanceof ClientPlayerEntity clientPlayerEntity) {
						clientPlayerEntity.networkHandler.getConnection().disconnect(Text.translatable("multiplayer.disconnect.invalid_packet"));
					}
					return null;
				})
		);
	}
	public ImmutableS2CProfileIndependentMessage(PlayerEntity player, boolean isOnClientSide, Text message, MessageType.Parameters messageType) {
		super(player, isOnClientSide);
		this.message = message;
		this.messageType = messageType;
	}

	@Override
	public @NotNull EnumSet<QuiltMessageType> getTypes() {
		return EnumSet.of(QuiltMessageType.PROFILE_INDEPENDENT, QuiltMessageType.SERVER, QuiltMessageType.OUTBOUND);
	}

	@Override
	public @NotNull ImmutableS2CProfileIndependentMessage immutableCopy() {
		return new ImmutableS2CProfileIndependentMessage(player, isOnClientSide, message, messageType);
	}

	@Override
	public @NotNull ProfileIndependentMessageS2CPacket asPacket() {
		return new ProfileIndependentMessageS2CPacket(message, messageType.serialize(player.world.getRegistryManager()));
	}

	public Text getMessage() {
		return message;
	}

	public MessageType.Parameters getMessageType() {
		return messageType;
	}
}
