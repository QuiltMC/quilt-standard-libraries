package org.quiltmc.qsl.worldgen.dimension.api;

import com.google.common.base.Preconditions;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.TeleportTarget;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.worldgen.dimension.impl.QuiltDimensionsImpl;

/**
 * This class contains methods that operate on world dimensions.
 */
public final class QuiltDimensions {

	// Static only-class, no instantiation necessary!
	private QuiltDimensions() {
	}

	@Nullable
	public static <E extends Entity> E teleport(@NotNull Entity entity, @NotNull ServerWorld destinationWorld, @NotNull TeleportTarget location) {
		Preconditions.checkNotNull(entity, "entity may not be null");
		Preconditions.checkNotNull(destinationWorld, "destinationWorld may not be null");
		Preconditions.checkNotNull(location, "location may not be null");
		Preconditions.checkArgument(!destinationWorld.isClient, "This method may only be called from the server side");

		return QuiltDimensionsImpl.teleport(entity, destinationWorld, location);
	}

}
