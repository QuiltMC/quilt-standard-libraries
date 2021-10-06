/*
 * Copyright (c) 2016, 2017, 2018, 2019 FabricMC, modifications 2021 QuiltMC
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

package org.quiltmc.qsl.commands.api.client;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Manages client-sided commands and provides some related helper methods, analagous to {@link net.minecraft.server.command.CommandManager}.
 *
 * <p>Client-sided commands are executed wholly on the client,
 * so players can use them in both singleplayer and multiplayer.
 *
 * <p>Command registrations should be done with {@link ClientCommandRegistrationCallback}.
 *
 * <p>The commands are run on the client game thread by default.
 * Avoid doing any heavy calculations here as that can freeze the game's rendering.
 * To mitigate this, you can move heavy code to another thread.
 *
 * <p>This class also has alternatives to the server-side helper methods in
 * {@link net.minecraft.server.command.CommandManager}:
 * {@link #literal(String)} and {@link #argument(String, ArgumentType)}.
 *
 * <p>The precedence rules of client-sided and server-sided commands with the same name
 * are an implementation detail that is not guaranteed to remain the same in future versions.
 * The aim is to make commands from the server take precedence over client-sided commands
 * in a future version of this API.
 *
 * <h2>Example command</h2>
 * <pre>
 * {@code
 * ClientCommandRegistrationCallback.EVENT.register(dispatcher ->
 *   dispatcher.register(
 *     ClientCommandManager.literal("hello").executes(context -> {
 *       context.getSource().sendFeedback(new LiteralText("Hello, world!"));
 *       return 0;
 *     })
 *   )
 * );
 * }
 * </pre>
 */
@Environment(EnvType.CLIENT)
public final class ClientCommandManager {
	/**
	 * The command dispatcher that handles client command registration and execution.
	 */
	public static final CommandDispatcher<QuiltClientCommandSource> DISPATCHER = new CommandDispatcher<>();

	private ClientCommandManager() {
	}

	/**
	 * Creates a literal argument builder.
	 *
	 * @param name the literal name
	 * @return the created argument builder
	 *
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
	 *
	 * @see RequiredArgumentBuilder#argument(String, ArgumentType)
	 * @see net.minecraft.server.command.CommandManager#argument(String, ArgumentType)
	 */
	public static <T> RequiredArgumentBuilder<QuiltClientCommandSource, T> argument(String name, ArgumentType<T> type) {
		return RequiredArgumentBuilder.argument(name, type);
	}
}
