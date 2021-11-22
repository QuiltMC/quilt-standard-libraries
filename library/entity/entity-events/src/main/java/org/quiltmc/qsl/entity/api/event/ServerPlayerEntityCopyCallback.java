package org.quiltmc.qsl.entity.api.event;

import net.minecraft.server.network.ServerPlayerEntity;
import org.quiltmc.qsl.base.api.event.ArrayEvent;

/**
 * A callback which is called on the logical server when a player is copied.
 *
 * <p>Players are copied on death and when returning from the end. The {@code wasDeath} parameter can be used to
 * differentiate between the two situations.
 *
 * <p>The callback is called after vanilla has done its own copying logic.
 *
 * @see ServerPlayerEntity#copyFrom(ServerPlayerEntity, boolean)
 */
@FunctionalInterface
public interface ServerPlayerEntityCopyCallback {
	/**
	 * Invoked when a player is copied on the logical server.
	 */
	ArrayEvent<ServerPlayerEntityCopyCallback> EVENT = ArrayEvent.create(ServerPlayerEntityCopyCallback.class, callbacks -> (newPlayer, original, wasDeath) -> {
		for (var callback : callbacks) {
			callback.onPlayerCopy(newPlayer, original, wasDeath);
		}
	});


	/**
	 * Called when a player is copied.
	 *
	 * @param newPlayer the new ServerPlayerEntity instance
	 * @param original 	the original ServerPlayerEntity instance
	 * @param wasDeath 	true if the copying is due to the player dying, false otherwise
	 */
	void onPlayerCopy(ServerPlayerEntity newPlayer, ServerPlayerEntity original, boolean wasDeath);
}
