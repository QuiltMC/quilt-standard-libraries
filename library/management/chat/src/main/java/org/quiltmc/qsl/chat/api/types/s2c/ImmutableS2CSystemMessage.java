package org.quiltmc.qsl.chat.api.types.s2c;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.SystemMessageS2CPacket;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.ImmutableAbstractMessage;

import java.util.EnumSet;

public class ImmutableS2CSystemMessage extends ImmutableAbstractMessage<ImmutableS2CSystemMessage, SystemMessageS2CPacket> {
	private final PlayerEntity player;
	private final Text content;
	private final boolean overlay;

	public ImmutableS2CSystemMessage(PlayerEntity player, SystemMessageS2CPacket packet) {
		this(
				player,
				packet.content(),
				packet.overlay()
		);
	}

	public ImmutableS2CSystemMessage(PlayerEntity player, Text content, boolean overlay) {
        super(player);
        this.player = player;
		this.content = content;
		this.overlay = overlay;
	}
	@Override
	public @NotNull EnumSet<QuiltMessageType> getTypes() {
		return EnumSet.of(QuiltMessageType.SYSTEM, QuiltMessageType.SERVER, QuiltMessageType.OUTBOUND);
	}

	@Override
	public @NotNull ImmutableS2CSystemMessage immutableCopy() {
		return new ImmutableS2CSystemMessage(player, content, overlay);
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
}
