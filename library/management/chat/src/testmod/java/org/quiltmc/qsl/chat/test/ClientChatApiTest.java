/*
 * Copyright 2023 QuiltMC
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

package org.quiltmc.qsl.chat.test;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.chat.api.QuiltChatEvents;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.ChatC2SMessage;
import org.quiltmc.qsl.chat.api.types.CommandC2SMessage;
import org.quiltmc.qsl.command.api.client.ClientCommandManager;
import org.quiltmc.qsl.command.api.client.ClientCommandRegistrationCallback;

import java.util.EnumSet;
import java.util.Random;

public class ClientChatApiTest implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) -> {
			// TODO: Something is messing up the signature that *ISNT* LSMT, message chain packer maybe?
			dispatcher.register(ClientCommandManager.literal("quilt_chat_api_testmod_client")
				.then(ClientCommandManager.literal("random_signed_chat_cancel")
					.then(ClientCommandManager.literal("inbound").executes(context -> {
						QuiltChatEvents.CANCEL.register(EnumSet.of(QuiltMessageType.CHAT, QuiltMessageType.INBOUND), abstractMessage -> {
							if (!(abstractMessage instanceof ChatC2SMessage) && !(abstractMessage instanceof CommandC2SMessage)) return false;
							if (new Random().nextBoolean()) {
								System.out.println("QC|TEST Cancelling inbound message");
								return true;
							} else {
								return false;
							}
						});
						return 0;
					}))
					.then(ClientCommandManager.literal("outbound").executes(context -> {
						QuiltChatEvents.CANCEL.register(EnumSet.of(QuiltMessageType.CHAT, QuiltMessageType.OUTBOUND), abstractMessage -> {
							if (!(abstractMessage instanceof ChatC2SMessage) && !(abstractMessage instanceof CommandC2SMessage)) return false;
							if (new Random().nextBoolean()) {
								System.out.println("QC|TEST Cancelling outbound message");
								return true;
							} else {
								return false;
							}
						});
						return 0;
					}))));
		});
	}
}
