package org.quiltmc.qsl.chat.api.types;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.SystemMessageS2CPacket;
import net.minecraft.text.Text;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.impl.InternalQuiltChatApiUtil;

import java.util.EnumSet;

public final class MutableS2CSystemMessage extends MutableAbstractMessage<ImmutableS2CSystemMessage, SystemMessageS2CPacket> {
	private Text content;
	private boolean overlay;

	public MutableS2CSystemMessage(PlayerEntity player, boolean isOnClientSide, SystemMessageS2CPacket packet) {
		this(
				player,
				isOnClientSide,
				packet.content(),
				packet.overlay()
		);
	}

	public MutableS2CSystemMessage(PlayerEntity player, boolean isOnClientSide, Text content, boolean overlay) {
		super(player, isOnClientSide);
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

	public void setContent(Text content) {
		this.content = content;
	}

	public boolean isOverlay() {
		return overlay;
	}

	public void setOverlay(boolean overlay) {
		this.overlay = overlay;
	}
}
