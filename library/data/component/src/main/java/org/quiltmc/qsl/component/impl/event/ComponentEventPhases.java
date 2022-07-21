package org.quiltmc.qsl.component.impl.event;

import net.minecraft.util.Identifier;
import org.quiltmc.qsl.component.impl.CommonInitializer;

public final class ComponentEventPhases {
	public static final Identifier TICK_LEVEL_CONTAINER = CommonInitializer.id("level_container_tick");
	public static final Identifier TICK_WORLD_CONTAINER = CommonInitializer.id("world_container_tick");
	public static final Identifier CLIENT_REQUEST_POLL = CommonInitializer.id("poll_sync_requests");
	public static final Identifier FREEZE_COMPONENT_REGISTRIES = CommonInitializer.id("freeze_component_registries");
	public static final Identifier UNFREEZE_COMPONENT_NETWORK = CommonInitializer.id("unfreeze_component_network");
	public static final Identifier FREEZE_COMPONENT_NETWORK = CommonInitializer.id("freeze_component_network");
	public static final Identifier SYNC_COMPONENT_REGISTRY = CommonInitializer.id("component_registry_sync");
}
