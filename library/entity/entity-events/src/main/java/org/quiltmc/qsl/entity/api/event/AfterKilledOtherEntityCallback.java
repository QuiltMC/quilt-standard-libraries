package org.quiltmc.qsl.entity.api.event;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.quiltmc.qsl.base.api.event.ArrayEvent;

/**
 * A callback which is invoked when an entity is killed by another entity.
 */
@FunctionalInterface
public interface AfterKilledOtherEntityCallback {
	/**
	 * Called when an entity is killed by another entity.
	 */
	ArrayEvent<AfterKilledOtherEntityCallback> EVENT = ArrayEvent.create(AfterKilledOtherEntityCallback.class, callbacks -> (world, killer, killed) -> {
		for (AfterKilledOtherEntityCallback callback : callbacks) {
			callback.afterKilledOtherEntity(world, killer, killed);
		}
	});


	/**
	 * Called when an entity is killed by another entity.
	 *
	 * @param world the world
	 * @param killer the entity
	 * @param killed the entity which was killed by the {@code killer}
	 */
	void afterKilledOtherEntity(World world, Entity killer, LivingEntity killed);
}
