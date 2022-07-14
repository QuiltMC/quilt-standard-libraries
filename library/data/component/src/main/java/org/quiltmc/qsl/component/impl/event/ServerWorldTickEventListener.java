package org.quiltmc.qsl.component.impl.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import org.quiltmc.qsl.base.api.event.ListenerPhase;
import org.quiltmc.qsl.component.impl.CommonInitializer;
import org.quiltmc.qsl.component.impl.util.ComponentProviderState;
import org.quiltmc.qsl.lifecycle.api.event.ServerWorldTickEvents;

@SuppressWarnings("unused")
@ListenerPhase(
		callbackTarget = ServerWorldTickEvents.End.class,
		namespace = CommonInitializer.MOD_ID, path = "tick_world_container"
)
public class ServerWorldTickEventListener implements ServerWorldTickEvents.End {
	@Override
	public void endWorldTick(MinecraftServer server, ServerWorld world) {
		ComponentProviderState.get(world).getComponentContainer().tick(world);
	}
}
