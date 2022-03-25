package org.quiltmc.qsl.worldgen.dimension.access;

import net.minecraft.world.TeleportTarget;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface EntityAccess {

	void setTeleportTarget(TeleportTarget target);

}
