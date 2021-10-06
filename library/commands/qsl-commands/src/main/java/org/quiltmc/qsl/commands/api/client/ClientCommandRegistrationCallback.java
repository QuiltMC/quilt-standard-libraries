package org.quiltmc.qsl.commands.api.client;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import org.quiltmc.qsl.base.api.event.ArrayEvent;
import org.quiltmc.qsl.commands.api.CommandRegistrationCallback;

/**
 * Invoked when client commands are registered.
 * @see #registerCommands(CommandDispatcher)
 * @see CommandRegistrationCallback
 */
@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface ClientCommandRegistrationCallback {
	ArrayEvent<ClientCommandRegistrationCallback> EVENT = ArrayEvent.create(ClientCommandRegistrationCallback.class, callbacks -> dispatcher -> {
		for (var callback : callbacks) {
			callback.registerCommands(dispatcher);
		}
	});

	/**
	 * @param dispatcher The {@link CommandDispatcher} to register commands with.
	 * @see ClientCommandRegistrationCallback
	 */
	void registerCommands(CommandDispatcher<QuiltClientCommandSource> dispatcher);
}
