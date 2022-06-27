package org.quiltmc.qsl.component.impl.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.SaveProperties;
import net.minecraft.world.level.LevelProperties;
import org.quiltmc.qsl.base.api.event.ListenerPhase;
import org.quiltmc.qsl.lifecycle.api.event.ServerTickEvents;

@ListenerPhase(
		callbackTarget = ServerTickEvents.End.class,
		namespace = "quilt_component", path = "tick_level_container"
)
public class ServerTickEventListener implements ServerTickEvents.End {
	@Override
	public void endServerTick(MinecraftServer server) {
		SaveProperties saveProperties = server.getSaveProperties();
		if (saveProperties instanceof LevelProperties levelProperties) {
			levelProperties.getContainer().tick(levelProperties);
		}
	}
}
