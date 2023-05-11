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

import net.minecraft.text.Text;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.chat.api.QuiltChatEvents;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.ChatC2SMessage;
import org.quiltmc.qsl.chat.api.types.ChatS2CMessage;
import org.quiltmc.qsl.chat.api.types.RawChatC2SMessage;
import org.quiltmc.qsl.chat.api.types.SystemS2CMessage;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.EnumSet;
import java.util.Random;

public class ChatApiTest implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
		MixinEnvironment.getCurrentEnvironment().setOption(MixinEnvironment.Option.DEBUG_EXPORT, true);

		//QuiltChatEvents.AFTER_PROCESS.register(EnumSet.allOf(QuiltMessageType.class), System.out::println);
		//QuiltChatEvents.BEFORE_PROCESS.register(EnumSet.allOf(QuiltMessageType.class), message -> {
		//	System.out.println(message.getTypes());
		//});

		QuiltChatEvents.BEFORE_PROCESS.register(EnumSet.of(QuiltMessageType.CHAT), abstractMessage -> {
			if (abstractMessage instanceof ChatC2SMessage chat) {
				System.out.println(chat.getTypes() + " " + chat.getMessageAcknowledgments());
			} else if (abstractMessage instanceof ChatS2CMessage chat) {
				System.out.println(chat.getTypes() + " " + chat.getBody().lastSeen());
			}
		});

		QuiltChatEvents.MODIFY.register(EnumSet.of(QuiltMessageType.CHAT, QuiltMessageType.CLIENT, QuiltMessageType.OUTBOUND), abstractMessage -> {
			if (abstractMessage instanceof RawChatC2SMessage raw) {
				return raw.withMessage(raw.getMessage() + ", wow!");
			}
			return abstractMessage;
		});

		QuiltChatEvents.MODIFY.register(EnumSet.of(QuiltMessageType.SYSTEM, QuiltMessageType.SERVER, QuiltMessageType.OUTBOUND), abstractMessage -> {
			if (abstractMessage instanceof SystemS2CMessage systemS2CMessage) {
				Text content = systemS2CMessage.getContent();
				if (new Random().nextBoolean()) {
					return systemS2CMessage.withContent(content.copy().append(Text.literal(", uwu")));
				}
			}

			return abstractMessage;
		});

		final boolean[] didEnableCrashing = {false};
		QuiltChatEvents.CANCEL.register(EnumSet.of(QuiltMessageType.CHAT, QuiltMessageType.CLIENT, QuiltMessageType.OUTBOUND), abstractMessage -> {
			if (abstractMessage instanceof RawChatC2SMessage chatC2SMessage) {
				if (chatC2SMessage.getMessage().startsWith("!register_crashing")) {
					if (!didEnableCrashing[0]) {
						didEnableCrashing[0] = true;
						registerBadEvents();
						return true;
					}
				}
			}

			return false;
		});

		final boolean[] didEnableStress = {false};
		QuiltChatEvents.CANCEL.register(EnumSet.of(QuiltMessageType.CHAT, QuiltMessageType.CLIENT, QuiltMessageType.OUTBOUND), abstractMessage -> {
			if (abstractMessage instanceof RawChatC2SMessage chatC2SMessage) {
				if (chatC2SMessage.getMessage().startsWith("!register_stress")) {
					if (!didEnableStress[0]) {
						didEnableStress[0] = true;
						registerStressEvents();
						return true;
					}
				}
			}

			return false;
		});
	}

	private void registerBadEvents() {
		QuiltChatEvents.MODIFY.register(EnumSet.allOf(QuiltMessageType.class), abstractMessage -> new SystemS2CMessage(abstractMessage.getPlayer(), abstractMessage.isClient(), Text.literal("im an evil event, muhahah"), false));
		QuiltChatEvents.MODIFY.register(EnumSet.allOf(QuiltMessageType.class), abstractMessage -> new Random().nextInt(3) == 0 ? null : abstractMessage );
	}

	private void registerStressEvents() {
		QuiltChatEvents.CANCEL.register(EnumSet.of(QuiltMessageType.CHAT, QuiltMessageType.INBOUND), abstractMessage -> {
			return new Random().nextBoolean();
		});
	}
}
