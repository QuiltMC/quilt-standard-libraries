package org.quiltmc.qsl.chat.impl.server;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.quiltmc.loader.api.minecraft.ClientOnly;

@ClientOnly
public class SystemMessageWrapper {
	private final ServerPlayerEntity target;
	private Text content;
	private boolean overlay;

	public SystemMessageWrapper(ServerPlayerEntity target, Text content, boolean overlay) {
		this.target = target;
		this.content = content;
		this.overlay = overlay;
	}

	public ServerPlayerEntity getTarget() { return target; }

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
