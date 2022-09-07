package org.quiltmc.qsl.entity_events.api.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.entity.Entity;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.client.ClientEventAwareListener;

/**
 * A callback that is invoked when an Entity is ticked on the logical client (nominally every 1/20 of a second).
 *
 * <p>There are two types of entity tick - standalone ({@link Entity#tick()}) and riding ({@link Entity#tickRiding()}).
 * This callback takes a parameter which specifies which type of tick it is.
 */
@Environment(EnvType.CLIENT)
@FunctionalInterface
public interface ClientEntityTickCallback extends ClientEventAwareListener {
	/**
	 * Called when an entity is ticked.
	 */
	Event<ClientEntityTickCallback> ENTITY_TICK = Event.create(ClientEntityTickCallback.class, callbacks -> (entity, isPassengerTick) -> {
		for (ClientEntityTickCallback callback : callbacks) {
			callback.onClientEntityTick(entity, isPassengerTick);
		}
	});

	/**
	 * Called when an entity is ticked on the logical client.
	 *
	 * @param entity the entity
	 * @param isPassengerTick whether the entity is being ticked as a passenger of another entity
	 */
	void onClientEntityTick(Entity entity, boolean isPassengerTick);
}
