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

import net.minecraft.network.packet.s2c.play.MessageRemovalS2CPacket;
import net.minecraft.server.command.CommandManager;
import net.minecraft.text.Text;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.chat.api.QuiltChatEvents;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.*;
import org.quiltmc.qsl.command.api.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.EnumSet;
import java.util.Random;

public class ChatApiTest implements ModInitializer {
	private static final Logger MODIFY_LOGGER = LoggerFactory.getLogger("QuiltChat|MODIFY");
	private static final Logger CANCEL_LOGGER = LoggerFactory.getLogger("QuiltChat|CANCEL");
	private static final Logger CANCELLED_LOGGER = LoggerFactory.getLogger("QuiltChat|CANCELLED");
	private static final Logger BEFORE_LOGGER = LoggerFactory.getLogger("QuiltChat|BEFORE_PROCESS");
	private static final Logger AFTER_LOGGER = LoggerFactory.getLogger("QuiltChat|AFTER_PROCESS");

	@Override
	public void onInitialize(ModContainer mod) {
		MixinEnvironment.getCurrentEnvironment().setOption(MixinEnvironment.Option.DEBUG_EXPORT, true);

		CommandRegistrationCallback.EVENT.register((dispatcher, buildContext, environment) -> {
			dispatcher.register(CommandManager.literal("quilt_chat_api_testmod")
				.then(CommandManager.literal("log_events")
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
					.then(CommandManager.literal("random_signed_chat_cancel")
						.then(CommandManager.literal("inbound").executes(context -> {
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
						.then(CommandManager.literal("outbound").executes(context -> {
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
						})))
				.then(CommandManager.literal("register_crashing_events")
					.then(CommandManager.literal("null_return").executes(context -> {
						QuiltChatEvents.MODIFY.register(EnumSet.noneOf(QuiltMessageType.class), abstractMessage -> null);
						return 0;
					}))
					.then(CommandManager.literal("bad_type_return").executes(context -> {
						QuiltChatEvents.MODIFY.register(EnumSet.noneOf(QuiltMessageType.class), abstractMessage -> {
							if (abstractMessage instanceof RawChatC2SMessage) {
								return new SystemS2CMessage(null, true, Text.empty(), false);
							} else {
								return new RemovalS2CMessage(null, true, (MessageRemovalS2CPacket) null);
							}
						});
						return 0;
					}))));
		});
	}
}
