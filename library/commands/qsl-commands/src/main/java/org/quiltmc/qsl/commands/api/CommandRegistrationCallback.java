/*
 * Copyright 2021 QuiltMC
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
