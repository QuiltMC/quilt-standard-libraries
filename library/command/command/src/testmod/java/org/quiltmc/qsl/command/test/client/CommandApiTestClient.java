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

package org.quiltmc.qsl.command.test.client;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.text.LiteralText;
import org.quiltmc.qsl.command.api.client.ClientCommandManager;
import org.quiltmc.qsl.command.api.client.ClientCommandRegistrationCallback;

public class CommandApiTestClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		ClientCommandRegistrationCallback.EVENT.register(dispatcher -> {
			dispatcher.register(
					ClientCommandManager.literal("test_client_command")
							.executes(ctx -> {
								ctx.getSource().sendFeedback(new LiteralText("It works!"));
								return 0;
							})
			);

			dispatcher.register(
					ClientCommandManager.literal("overrideme")
							.executes(ctx -> {
								ctx.getSource().sendFeedback(new LiteralText("Client!"));
								return 0;
							})
			);
		});
	}
}
