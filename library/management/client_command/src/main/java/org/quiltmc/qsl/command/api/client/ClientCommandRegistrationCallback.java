/*
 * Copyright 2022 The Quilt Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.command.api.client;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandBuildContext;
import net.minecraft.server.command.CommandManager;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.client.ClientEventAwareListener;

/**
 * Callback for registering client-side commands.
 *
 * @see ClientCommandManager
 */
@FunctionalInterface
@ClientOnly
public interface ClientCommandRegistrationCallback extends ClientEventAwareListener {
	/**
	 * Event invoked when client-sided commands are registered.
	 */
	Event<ClientCommandRegistrationCallback> EVENT = Event.create(ClientCommandRegistrationCallback.class, callbacks -> (dispatcher, buildContext, environment) -> {
		for (var callback : callbacks) {
			callback.registerCommands(dispatcher, buildContext, environment);
		}
	});

	/**
	 * Called when client-side commands are registered.
	 *
	 * @param dispatcher   the command dispatcher
	 * @param buildContext the command build context
	 * @param environment  the registration environment, allows registration of single-player-only commands or dedicated-only commands
	 */
	void registerCommands(CommandDispatcher<QuiltClientCommandSource> dispatcher, CommandBuildContext buildContext, CommandManager.RegistrationEnvironment environment);
}
