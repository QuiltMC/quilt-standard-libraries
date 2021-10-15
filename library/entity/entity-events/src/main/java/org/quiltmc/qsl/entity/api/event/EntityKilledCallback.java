package org.quiltmc.qsl.entity.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.base.api.event.ArrayEvent;

/**
 * A callback which is invoked on the logical server when an entity is killed by another entity.
 */
@FunctionalInterface
public interface EntityKilledCallback {
	/**
	 * Called when an entity is killed by another entity.
	 */
	ArrayEvent<EntityKilledCallback> EVENT = ArrayEvent.create(EntityKilledCallback.class, callbacks -> (world, killer, killed) -> {
		for (EntityKilledCallback callback : callbacks) {
			callback.onKilled(world, killer, killed);
		}
	});


	/**
	 * Called when an entity is killed by another entity.
	 *
	 * @param world the world
	 * @param killer the killing entity, possibly null
	 * @param killed the entity which was killed by the {@code killer}
	 */
	void onKilled(World world, @Nullable Entity killer, LivingEntity killed);
}
