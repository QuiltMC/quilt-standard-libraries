package org.quiltmc.qsl.commands.api;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import org.quiltmc.qsl.base.api.event.ArrayEvent;

@FunctionalInterface
public interface CommandRegistrationCallback {
	ArrayEvent<CommandRegistrationCallback> EVENT = ArrayEvent.create(CommandRegistrationCallback.class, callbacks -> (dispatcher, env) -> {
		for (var callback : callbacks) {
			callback.registerCommands(dispatcher, env);
		}
	});

	void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandManager.RegistrationEnvironment env);
}
