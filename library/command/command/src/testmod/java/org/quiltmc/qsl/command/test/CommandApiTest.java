/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.command.test;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.loader.api.FabricLoader;

import net.minecraft.command.CommandBuildContext;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;

import org.quiltmc.qsl.command.api.CommandRegistrationCallback;

public class CommandApiTest implements CommandRegistrationCallback {
	@Override
	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandBuildContext context, CommandManager.RegistrationEnvironment environment) {
		if (environment.isDedicated()) {
			dispatcher.register(literal("ping")
					.executes(ctx -> {
						ctx.getSource().sendFeedback(new LiteralText("pong!"), false);
						return 0;
					})
			);
		} else if (environment.isIntegrated()) {
			dispatcher.register(literal("singleplayer_only")
					.executes(ctx -> {
						ctx.getSource().sendFeedback(new LiteralText("This command should only exist in singleplayer"), false);
						return 0;
					})
			);
		}

		dispatcher.register(literal("quilt")
				.executes(ctx -> {
					//noinspection OptionalGetWithoutIsPresent
					ctx.getSource().sendFeedback(new LiteralText("Quilt Version: " + FabricLoader.getInstance().getModContainer("quilt_base").get().getMetadata().getVersion().getFriendlyString()), false);
					return 0;
				})
		);

		dispatcher.register(literal("test_with_arg")
				.then(literal("item")
						.then(argument("arg", ItemStackArgumentType.itemStack(context))
								.executes(ctx -> {
									ItemStackArgument arg = ItemStackArgumentType.getItemStackArgument(ctx, "arg");
									ctx.getSource().sendFeedback(new LiteralText("Ooohh, you have chosen: " + arg.getItem() + "!"), false);
									return 0;
								}))
				)
		);
	}
}
