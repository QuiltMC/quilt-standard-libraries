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

	/**
	 * Directly teleports the specified entity to the specified location in another dimension,
	 * circumventing the built-in portal logic in vanilla.
	 *
	 * <p>Note: When teleporting a non-player entity to another dimension, it may be replaced with
	 * a new entity in the target dimension.
	 *
	 * @param entity the entity to teleport
	 * @param destinationWorld the dimension to teleport the entity to
	 * @param location the location to place the entity at after it is moved to the specified world.
	 * 		Just like in vanilla, the velocity is ignored. If this location is set to {@code null},
	 * 		the entity will not be teleported.
	 * @param <E> the type of the entity that is being teleported
	 * @return the teleported entity in the destination dimension, which will either be a new entity or teleported,
	 * 		depending on the type of entity
	 * @apiNote this method may only be called on the main server thread
	 */
	@Nullable
	public static <E extends Entity> E teleport(@NotNull Entity entity, @NotNull ServerWorld destinationWorld, @Nullable TeleportTarget location) {
		Preconditions.checkNotNull(entity, "entity may not be null");
		Preconditions.checkNotNull(destinationWorld, "destinationWorld may not be null");
		Preconditions.checkArgument(!destinationWorld.isClient, "This method may only be called from the server side");

		return QuiltDimensionsImpl.teleport(entity, destinationWorld, location);
	}

}
