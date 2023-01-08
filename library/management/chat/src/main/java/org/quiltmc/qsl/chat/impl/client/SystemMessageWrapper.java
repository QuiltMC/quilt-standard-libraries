package org.quiltmc.qsl.chat.impl.client;

import net.minecraft.network.packet.s2c.play.SystemMessageS2CPacket;
import net.minecraft.text.Text;
import org.quiltmc.loader.api.minecraft.ClientOnly;

@ClientOnly
public class SystemMessageWrapper {
	private Text content;
	private boolean overlay;

	public SystemMessageWrapper(SystemMessageS2CPacket packet) {
		this(packet.content(), packet.overlay());
	}

	public SystemMessageWrapper(Text content, boolean overlay) {
		this.content = content;
		this.overlay = overlay;
	}

	public SystemMessageS2CPacket asPacket() {
		return new SystemMessageS2CPacket(content, overlay);
	}

	public Text getContext() {
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
