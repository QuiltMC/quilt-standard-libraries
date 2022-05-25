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

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Formatting;

import org.quiltmc.loader.api.QuiltLoader;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.quiltmc.qsl.command.api.EnumArgumentType;

public class CommandApiTest implements CommandRegistrationCallback {
	private static final EnumArgumentType ENUM_ARGUMENT_TYPE = EnumArgumentType.enumConstant(TestEnum.class);

	@Override
	public void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher, boolean integrated, boolean dedicated) {
		if (dedicated) {
			dispatcher.register(literal("ping")
					.executes(ctx -> {
						ctx.getSource().sendFeedback(new LiteralText("pong!"), false);
						return 0;
					})
			);
		} else if (integrated) {
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
					ctx.getSource().sendFeedback(new LiteralText("Quilt Version: " + QuiltLoader.getModContainer("quilt_base").get().metadata().version().raw()), false);
					return 0;
				})
				.then(literal("enum_arg")
						.then(argument("enum", ENUM_ARGUMENT_TYPE)
								.executes(ctx -> {
									var arg = EnumArgumentType.getEnumConstant(ctx, "enum", TestEnum.class);
									ctx.getSource().sendFeedback(
											new LiteralText("Got: ").append(new LiteralText(arg.toString()).formatted(Formatting.GOLD)),
											false
									);
									return 0;
								})
						)
				)
		);
	}
}
