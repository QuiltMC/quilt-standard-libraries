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

package org.quiltmc.qsl.command.test;

import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandBuildContext;
import net.minecraft.command.argument.ItemStackArgument;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.command.api.EnumArgumentType;

public class CommandApiTest implements CommandRegistrationCallback {
	private static final EnumArgumentType ENUM_ARGUMENT_TYPE = EnumArgumentType.enumConstant(TestEnum.class);

	@Override
	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, CommandBuildContext context, CommandManager.RegistrationEnvironment environment) {
		if (environment.isDedicated()) {
			dispatcher.register(literal("ping")
					.executes(ctx -> {
						ctx.getSource().sendSystemMessage(Text.literal("pong!"));
						return 0;
					})
			);
		} else if (environment.isIntegrated()) {
			dispatcher.register(literal("singleplayer_only")
					.executes(ctx -> {
						ctx.getSource().sendSystemMessage(Text.literal("This command should only exist in singleplayer"));
						return 0;
					})
			);
		}

		dispatcher.register(literal("quilt")
				.executes(ctx -> {
					//noinspection OptionalGetWithoutIsPresent
					ctx.getSource().sendSystemMessage(Text.literal(
							"Quilt Version: " + QuiltLoader.getModContainer("quilt_base").get().metadata().version().raw()
					));
					return 0;
				})
				.then(literal("enum_arg")
						.then(argument("enum", ENUM_ARGUMENT_TYPE)
								.executes(ctx -> {
									var arg = EnumArgumentType.getEnumConstant(ctx, "enum", TestEnum.class);
									ctx.getSource().sendSystemMessage(
											Text.literal("Got: ").append(Text.literal(arg.toString()).formatted(Formatting.GOLD))
									);
									return 0;
								})
						)
				)
		);

		dispatcher.register(literal("test_with_arg")
				.then(literal("item")
						.then(argument("arg", ItemStackArgumentType.itemStack(context))
								.executes(ctx -> {
									ItemStackArgument arg = ItemStackArgumentType.getItemStackArgument(ctx, "arg");
									ctx.getSource().sendSystemMessage(Text.literal("Ooohh, you have chosen: " + arg.getItem() + "!"));
									return 0;
								}))
				)
		);
	}
}
