package org.quiltmc.qsl.entity.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import org.quiltmc.qsl.base.api.event.ArrayEvent;

/**
 * A callback that is invoked when an Entity is ticked (nominally every 1/20 of a second).
 *
 * <p>There are two types of entity tick - standalone ({@link Entity#tick()}) and riding ({@link Entity#tickRiding()}).
 * This callback takes a parameter which
 */
@FunctionalInterface
public interface EntityTickCallback {
	/**
	 * Called when an entity is ticked.
	 */
	ArrayEvent<EntityTickCallback> ENTITY_TICK = ArrayEvent.create(EntityTickCallback.class, callbacks -> (entity, isWorldClient, isPassengerTick) -> {
		for (EntityTickCallback callback : callbacks) {
			callback.onTick(entity, isWorldClient, isPassengerTick);
		}
	});

	/**
	 * Called when an entity is ticked.
	 *
	 * @param entity the entity
	 * @param isWorldClient whether the entity's world is a client world
	 * @param isPassengerTick whether the entity is being ticked as a passenger of another entity
	 */
	void onTick(Entity entity, boolean isWorldClient, boolean isPassengerTick);
}
