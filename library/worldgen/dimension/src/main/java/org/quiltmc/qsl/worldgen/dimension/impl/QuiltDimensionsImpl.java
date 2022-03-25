package org.quiltmc.qsl.worldgen.dimension.impl;

import com.google.common.base.Preconditions;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.worldgen.dimension.access.EntityAccess;

@ApiStatus.Internal
public class QuiltDimensionsImpl {

	// Static only-class, no instantiation necessary!
	private QuiltDimensionsImpl() {
	}

	@SuppressWarnings("unchecked")
	public static <E extends Entity> E teleport(Entity entity, ServerWorld destinationWorld, TeleportTarget location) {
		Preconditions.checkArgument(Thread.currentThread() == ((ServerWorld) entity.getWorld()).getServer().getThread(), "This method may only be called from the main server thread");

		EntityAccess access = (EntityAccess) entity;
		access.setTeleportTarget(location);

		try {
			return (E) entity.moveToWorld(destinationWorld);
		} finally {
			// Always clean up!
			access.setTeleportTarget(null);
		}
	}

}
