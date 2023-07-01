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

package org.quiltmc.qsl.command.client.test;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.command.CommandBuildContext;
import net.minecraft.command.argument.BlockStateArgument;
import net.minecraft.command.argument.BlockStateArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import org.quiltmc.qsl.command.api.client.ClientCommandManager;
import org.quiltmc.qsl.command.api.client.ClientCommandRegistrationCallback;
import org.quiltmc.qsl.command.api.client.QuiltClientCommandSource;

public class ClientCommandApiTest implements ClientCommandRegistrationCallback {
	@Override
	public void registerCommands(CommandDispatcher<QuiltClientCommandSource> dispatcher, CommandBuildContext buildContext,
			CommandManager.RegistrationEnvironment environment) {
		dispatcher.register(
				ClientCommandManager.literal("test_client_command")
						.executes(ctx -> {
							ctx.getSource().sendFeedback(Text.literal("It works!")
									.styled(style -> style.withClickEvent(
											new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/test_client_command with_arg minecraft:dirt")
									))
							);
							return 0;
						})
						.then(ClientCommandManager.literal("with_arg")
								.then(ClientCommandManager.argument("block", BlockStateArgumentType.blockState(buildContext))
										.executes(ctx -> {
											BlockStateArgument arg = ctx.getArgument("block", BlockStateArgument.class);
											ctx.getSource().sendFeedback(Text.literal("You have given me: ")
													.append(Text.literal(arg.getBlockState().toString()).formatted(Formatting.GOLD))
													.append("!"));

											return 0;
										})
								)
						)
		);

		dispatcher.register(
				ClientCommandManager.literal("seed")
						.executes(ctx -> {
							ctx.getSource().sendFeedback(Text.of("This is a client-only command which conflicts with a server command!"));
							return 0;
						})
		);
	}
}
