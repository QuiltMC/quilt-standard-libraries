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

package org.quiltmc.qsl.command.test;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.text.LiteralText;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;

import static net.minecraft.server.command.CommandManager.literal;

public class CommandApiTest implements ModInitializer {
	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, integrated, dedicated) -> {
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
						ctx.getSource().sendFeedback(new LiteralText("Quilt Version: "+FabricLoader.getInstance().getModContainer("quilt_base").get().getMetadata().getVersion().getFriendlyString()), false);
						return 0;
					})
			);

			dispatcher.register(literal("overrideme")
					.executes(ctx -> {
						ctx.getSource().sendFeedback(new LiteralText("Server!"), false);
						return 0;
					})
			);
		});
	}
}
