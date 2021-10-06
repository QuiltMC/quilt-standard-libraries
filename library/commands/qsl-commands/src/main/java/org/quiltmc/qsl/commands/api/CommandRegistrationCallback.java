package org.quiltmc.qsl.commands.api;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.ServerCommandSource;
import org.quiltmc.qsl.base.api.event.ArrayEvent;

/**
 * Invoked when commands are registered.
 */
@FunctionalInterface
public interface CommandRegistrationCallback {
	ArrayEvent<CommandRegistrationCallback> EVENT = ArrayEvent.create(CommandRegistrationCallback.class, callbacks -> (dispatcher, integrated, dedicated) -> {
		for (var callback : callbacks) {
			callback.registerCommands(dispatcher, integrated, dedicated);
		}
	});

	void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, boolean registerForIntegrated, boolean registerForDedicated);
}
