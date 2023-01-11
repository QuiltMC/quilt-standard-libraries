package org.quiltmc.qsl.chat.api.types;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.SystemMessageS2CPacket;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.impl.InternalQuiltChatApiUtil;

import java.util.EnumSet;

public final class ImmutableS2CSystemMessage extends ImmutableAbstractMessage<ImmutableS2CSystemMessage, SystemMessageS2CPacket> {
	private final PlayerEntity player;
	private final Text content;
	private final boolean overlay;

	public ImmutableS2CSystemMessage(PlayerEntity player, boolean isOnClientSide, SystemMessageS2CPacket packet) {
		this(
				player,
				isOnClientSide,
				packet.content(),
				packet.overlay()
		);
	}

	public ImmutableS2CSystemMessage(PlayerEntity player, boolean isOnClientSide, Text content, boolean overlay) {
        super(player, isOnClientSide);
        this.player = player;
		this.content = content;
		this.overlay = overlay;
	}
	@Override
	public @NotNull EnumSet<QuiltMessageType> getTypes() {
		return InternalQuiltChatApiUtil.s2cType(QuiltMessageType.SYSTEM, isOnClientSide);
	}

	@Override
	public @NotNull ImmutableS2CSystemMessage immutableCopy() {
		return new ImmutableS2CSystemMessage(player, isOnClientSide, content, overlay);
	}

	@Override
	public @NotNull SystemMessageS2CPacket asPacket() {
		return new SystemMessageS2CPacket(content, overlay);
	}

	public Text getContent() {
		return content;
	}

	public boolean isOverlay() {
		return overlay;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("ImmutableS2CSystemMessage{");
		sb.append("player=").append(player);
		sb.append(", content=").append(content);
		sb.append(", overlay=").append(overlay);
		sb.append(", player=").append(player);
		sb.append(", isOnClientSide=").append(isOnClientSide);
		sb.append('}');
		return sb.toString();
	}
}
