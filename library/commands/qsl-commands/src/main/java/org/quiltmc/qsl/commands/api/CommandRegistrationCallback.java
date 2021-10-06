package org.quiltmc.qsl.commands.api;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import org.quiltmc.qsl.base.api.event.ArrayEvent;

/**
 * Event for registering server-side commands.
 */
@FunctionalInterface
public interface CommandRegistrationCallback {
	/**
	 * Invoked when server-side commands are registered.
	 */
	ArrayEvent<CommandRegistrationCallback> EVENT = ArrayEvent.create(CommandRegistrationCallback.class, callbacks -> (dispatcher, integrated, dedicated) -> {
		for (var callback : callbacks) {
			callback.registerCommands(dispatcher, integrated, dedicated);
		}
	});

	/**
	 * Called when server-side commands are registered.
	 *
	 * @param dispatcher the command dispatcher.
	 * @param integrated whether commands intended for only the integrated server (i.e. singleplayer) should be registered.
	 * @param dedicated whether commands intended for only the dedicated server should be registered.
	 */
	void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, boolean integrated, boolean dedicated);
}
