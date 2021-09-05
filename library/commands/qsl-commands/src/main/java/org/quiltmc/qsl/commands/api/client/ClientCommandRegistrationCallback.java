package org.quiltmc.qsl.commands.api.client;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.server.command.ServerCommandSource;
import org.quiltmc.qsl.base.api.event.ArrayEvent;

@FunctionalInterface
@Environment(EnvType.CLIENT)
public interface ClientCommandRegistrationCallback {
	ArrayEvent<ClientCommandRegistrationCallback> EVENT = ArrayEvent.create(ClientCommandRegistrationCallback.class, callbacks -> dispatcher -> {
		for (var callback : callbacks) {
			callback.registerCommands(dispatcher);
		}
	});

	void registerCommands(CommandDispatcher<QuiltClientCommandSource> dispatcher);
}
