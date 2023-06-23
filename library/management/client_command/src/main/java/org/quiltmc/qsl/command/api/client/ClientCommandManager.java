/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.command.impl.client.ClientCommandInternals;

/**
 * Manages client-sided commands and provides some related helper methods, analogous to
 * {@link net.minecraft.server.command.CommandManager CommandManager}.
 * <p>
 * Client-sided commands are executed wholly on the client,
 * so players can use them in both singleplayer and multiplayer.
 * <p>
 * Command registrations should be done with {@link ClientCommandRegistrationCallback}.
 * <p>
 * The commands are run on the client game thread by default.
 * Avoid doing any heavy calculations here as that can freeze the game's rendering.
 * To mitigate this, you can move heavy code to another thread.
 * <p>
 * This class also has alternatives to the server-side helper methods in
 * {@link net.minecraft.server.command.CommandManager CommandManager}: {@link #literal(String)} and
 * {@link #argument(String, ArgumentType)}.
 * <p>
 * Server-sided commands have precedence over client-sided commands. Client-sided commands which are overridden by
 * server-sided ones can be run anyway with {@code /qcc run}.
 *
 * <h2>Example command</h2>
 * <pre>{@code
 * ClientCommandRegistrationCallback.EVENT.register(dispatcher ->
 *   dispatcher.register(
 *     ClientCommandManager.literal("hello").executes(context -> {
 *       context.getSource().sendFeedback(new LiteralText("Hello, world!"));
 *       return 0;
 *     })
 *   )
 * );
 * }</pre>
 */
@ClientOnly
public final class ClientCommandManager {
	private ClientCommandManager() {
	}

	/**
	 * {@return the command dispatcher that handles client command registration and execution}
	 *
	 * @see ClientCommandRegistrationCallback client command registration
	 */
	public static CommandDispatcher<QuiltClientCommandSource> getDispatcher() {
		return ClientCommandInternals.getDispatcher();
	}

	/**
	 * Creates a literal argument builder.
	 *
	 * @param name the literal name
	 * @return the created argument builder
	 * @see LiteralArgumentBuilder#literal(String)
	 * @see net.minecraft.server.command.CommandManager#literal(String)
	 */
	public static LiteralArgumentBuilder<QuiltClientCommandSource> literal(String name) {
		return LiteralArgumentBuilder.literal(name);
	}

	/**
	 * Creates a required argument builder.
	 *
	 * @param name the name of the argument
	 * @param type the type of the argument
	 * @param <T>  the type of the parsed argument value
	 * @return the created argument builder
	 * @see RequiredArgumentBuilder#argument(String, ArgumentType)
	 * @see net.minecraft.server.command.CommandManager#argument(String, ArgumentType)
	 */
	public static <T> RequiredArgumentBuilder<QuiltClientCommandSource, T> argument(String name, ArgumentType<T> type) {
		return RequiredArgumentBuilder.argument(name, type);
	}
}
