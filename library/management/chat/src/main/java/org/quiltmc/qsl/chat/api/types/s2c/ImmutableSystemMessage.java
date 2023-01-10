package org.quiltmc.qsl.chat.api.types.s2c;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.SystemMessageS2CPacket;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.ImmutableAbstractMessage;

import java.util.EnumSet;

public class ImmutableSystemMessage extends ImmutableAbstractMessage<ImmutableSystemMessage, SystemMessageS2CPacket> {
	private final PlayerEntity player;
	private final Text content;
	private final boolean overlay;

	public ImmutableSystemMessage(PlayerEntity player, Text content, boolean overlay) {
		this.player = player;
		this.content = content;
		this.overlay = overlay;
	}
	@Override
	public @NotNull EnumSet<QuiltMessageType> getTypes() {
		return EnumSet.of(QuiltMessageType.SYSTEM, QuiltMessageType.SERVER, QuiltMessageType.OUTBOUND);
	}

	@Override
	public @NotNull ImmutableSystemMessage immutableCopy() {
		return new ImmutableSystemMessage(player, content, overlay);
	}

	@Override
	public @NotNull SystemMessageS2CPacket packet() {
		return new SystemMessageS2CPacket(content, overlay);
	}


	public PlayerEntity getPlayer() {
		return player;
	}

	public Text getContent() {
		return content;
	}

	public boolean isOverlay() {
		return overlay;
	}
}
