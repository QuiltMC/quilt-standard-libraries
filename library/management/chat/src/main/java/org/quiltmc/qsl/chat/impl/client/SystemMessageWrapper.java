package org.quiltmc.qsl.chat.impl.client;

import net.minecraft.network.packet.s2c.play.SystemMessageS2CPacket;
import net.minecraft.text.Text;
import org.quiltmc.loader.api.minecraft.ClientOnly;

@ClientOnly
public class SystemMessageWrapper {
	private Text context;
	private boolean overlay;

	public SystemMessageWrapper(SystemMessageS2CPacket packet) {
		this(packet.content(), packet.overlay());
	}

	public SystemMessageWrapper(Text context, boolean overlay) {
		this.context = context;
		this.overlay = overlay;
	}

	public SystemMessageS2CPacket asPacket() {
		return new SystemMessageS2CPacket(context, overlay);
	}

	public Text getContext() {
		return context;
	}

	public void setContext(Text context) {
		this.context = context;
	}

	public boolean isOverlay() {
		return overlay;
	}

	public void setOverlay(boolean overlay) {
		this.overlay = overlay;
	}
}
