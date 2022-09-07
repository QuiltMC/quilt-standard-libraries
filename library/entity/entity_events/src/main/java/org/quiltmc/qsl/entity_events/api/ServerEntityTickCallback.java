package org.quiltmc.qsl.entity_events.api;

import net.minecraft.entity.Entity;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.EventAwareListener;

/**
 * A callback that is invoked when an Entity is ticked on the logical server (nominally every 1/20 of a second).
 *
 * <p>There are two types of entity tick - standalone ({@link Entity#tick()}) and riding ({@link Entity#tickRiding()}).
 * This callback takes a parameter which specifies which type of tick it is.
 */
@FunctionalInterface
public interface ServerEntityTickCallback extends EventAwareListener {
	/**
	 * Called when an entity is ticked.
	 */
	Event<ServerEntityTickCallback> ENTITY_TICK = Event.create(ServerEntityTickCallback.class, callbacks -> (entity, isPassengerTick) -> {
		for (ServerEntityTickCallback callback : callbacks) {
			callback.onServerEntityTick(entity, isPassengerTick);
		}
	});

	/**
	 * Called when an entity is ticked on the logical server.
	 *
	 * @param entity the entity
	 * @param isPassengerTick whether the entity is being ticked as a passenger of another entity
	 */
	void onServerEntityTick(Entity entity, boolean isPassengerTick);
}
