package org.quiltmc.qsl.commands.api.client;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.quiltmc.qsl.base.api.event.ArrayEvent;

/**
 * Event for registering client-side commands.
 *
 * @see ClientCommandManager
 */
@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface ClientCommandRegistrationCallback {
	/**
	 * Invoked when client-side commands are registered.
	 */
	ArrayEvent<ClientCommandRegistrationCallback> EVENT = ArrayEvent.create(ClientCommandRegistrationCallback.class, callbacks -> dispatcher -> {
		for (var callback : callbacks) {
			callback.registerCommands(dispatcher);
		}
	});

	/**
	 * Called when client-side commands are registered.
	 *
	 * @param dispatcher the command dispatcher.
	 */
	void registerCommands(CommandDispatcher<QuiltClientCommandSource> dispatcher);
}
