package org.quiltmc.qsl.commands.api;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import org.quiltmc.qsl.base.api.event.ArrayEvent;
import org.quiltmc.qsl.commands.api.client.ClientCommandRegistrationCallback;

/**
 * Invoked when commands are registered.
 * @see #registerCommands(CommandDispatcher, boolean, boolean)
 * @see ClientCommandRegistrationCallback
 */
@FunctionalInterface
public interface CommandRegistrationCallback {
	ArrayEvent<CommandRegistrationCallback> EVENT = ArrayEvent.create(CommandRegistrationCallback.class, callbacks -> (dispatcher, integrated, dedicated) -> {
		for (var callback : callbacks) {
			callback.registerCommands(dispatcher, integrated, dedicated);
		}
	});

	/**
	 * @param dispatcher The {@link CommandDispatcher} to register commands with.
	 * @param integrated Indicates if the command is to be registered on an integrated server.
	 * @param dedicated Indicates if the command should be registered on a dedicated server.
	 * @see CommandRegistrationCallback
	 */
	void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, boolean integrated, boolean dedicated);
}
