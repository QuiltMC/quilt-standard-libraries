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

import static org.quiltmc.qsl.chat.test.ChatApiTest.AFTER_LOGGER;
import static org.quiltmc.qsl.chat.test.ChatApiTest.BEFORE_LOGGER;
import static org.quiltmc.qsl.chat.test.ChatApiTest.CANCELLED_LOGGER;
import static org.quiltmc.qsl.chat.test.ChatApiTest.CANCEL_LOGGER;
import static org.quiltmc.qsl.chat.test.ChatApiTest.MODIFY_LOGGER;

import java.util.EnumSet;
import java.util.Random;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;
import org.quiltmc.qsl.chat.api.QuiltChatEvents;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.ChatC2SMessage;
import org.quiltmc.qsl.chat.api.types.CommandC2SMessage;
import org.quiltmc.qsl.chat.api.types.RawChatC2SMessage;
import org.quiltmc.qsl.chat.api.types.RemovalS2CMessage;
import org.quiltmc.qsl.chat.api.types.SystemS2CMessage;
import org.quiltmc.qsl.command.api.client.ClientCommandManager;
import org.quiltmc.qsl.command.api.client.ClientCommandRegistrationCallback;

import net.minecraft.network.packet.s2c.play.MessageRemovalS2CPacket;
import net.minecraft.text.Text;

public class ClientChatApiTest implements ClientModInitializer {
	@Override
	public void onInitializeClient(ModContainer mod) {
		ClientCommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) -> {
			dispatcher.register(ClientCommandManager.literal("quilt_chat_api_testmod_client")
				.then(ClientCommandManager.literal("log_events")
					.executes(context -> {
						QuiltChatEvents.MODIFY.register(EnumSet.noneOf(QuiltMessageType.class), abstractMessage -> {
							MODIFY_LOGGER.info(abstractMessage.toString());
							return abstractMessage;
						});
						QuiltChatEvents.CANCEL.register(EnumSet.noneOf(QuiltMessageType.class), abstractMessage -> {
							CANCEL_LOGGER.info(abstractMessage.toString());
							return false;
						});
						QuiltChatEvents.CANCELLED.register(EnumSet.noneOf(QuiltMessageType.class), abstractMessage -> {
							CANCELLED_LOGGER.info(abstractMessage.toString());
						});
						QuiltChatEvents.BEFORE_PROCESS.register(EnumSet.noneOf(QuiltMessageType.class), abstractMessage -> {
							BEFORE_LOGGER.info(abstractMessage.toString());
						});
						QuiltChatEvents.AFTER_PROCESS.register(EnumSet.noneOf(QuiltMessageType.class), abstractMessage -> {
							AFTER_LOGGER.info(abstractMessage.toString());
						});
						return 0;
					}))
				.then(ClientCommandManager.literal("random_signed_chat_cancel")
					.then(ClientCommandManager.literal("inbound").executes(context -> {
						QuiltChatEvents.CANCEL.register(EnumSet.of(QuiltMessageType.CLIENT, QuiltMessageType.INBOUND), abstractMessage -> {
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
						QuiltChatEvents.CANCEL.register(EnumSet.of(QuiltMessageType.CLIENT, QuiltMessageType.OUTBOUND), abstractMessage -> {
							if (!(abstractMessage instanceof ChatC2SMessage) && !(abstractMessage instanceof CommandC2SMessage)) return false;
							if (new Random().nextBoolean()) {
								System.out.println("QC|TEST Cancelling outbound message");
								return true;
							} else {
								return false;
							}
						});
						return 0;
					})))
				.then(ClientCommandManager.literal("register_crashing_events")
					.then(ClientCommandManager.literal("null_return").executes(context -> {
						QuiltChatEvents.MODIFY.register(EnumSet.noneOf(QuiltMessageType.class), abstractMessage -> null);
						return 0;
					}))
					.then(ClientCommandManager.literal("bad_type_return").executes(context -> {
						QuiltChatEvents.MODIFY.register(EnumSet.noneOf(QuiltMessageType.class), abstractMessage -> {
							if (abstractMessage instanceof RawChatC2SMessage) {
								return new SystemS2CMessage(null, true, Text.empty(), false);
							} else {
								return new RemovalS2CMessage(null, true, (MessageRemovalS2CPacket) null);
							}
						});
						return 0;
					}))
					.then(ClientCommandManager.literal("exception").executes(context -> {
						QuiltChatEvents.MODIFY.register(EnumSet.noneOf(QuiltMessageType.class), abstractMessage -> {
							throw new RuntimeException("Exception generated by chat api client test mod");
						});
						return 0;
					}))));
		});
	}
}
