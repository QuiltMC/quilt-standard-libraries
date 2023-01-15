package org.quiltmc.qsl.chat.api.types;

import net.minecraft.entity.player.PlayerEntity;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.chat.api.QuiltMessageType;

import java.util.EnumSet;

public class RawChatC2SMessage extends AbstractChatMessage<String> {
	private final String message;

	public RawChatC2SMessage(@NotNull PlayerEntity player, boolean isOnClientSide, String message) {
		super(player, isOnClientSide);
		this.message = message;
	}

	@Override
	public @NotNull EnumSet<QuiltMessageType> getTypes() {
		return EnumSet.of(QuiltMessageType.CHAT, QuiltMessageType.CLIENT, QuiltMessageType.OUTBOUND);
	}

	public String getMessage() {
		return message;
	}

	public RawChatC2SMessage withMessage(String message) {
		return new RawChatC2SMessage(player, isOnClientSide, message);
	}

	@Override
	public @NotNull String serialized() {
		return message;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("RawChatC2SMessage{");
		sb.append("message='").append(message).append('\'');
		sb.append('}');
		return sb.toString();
	}
}
