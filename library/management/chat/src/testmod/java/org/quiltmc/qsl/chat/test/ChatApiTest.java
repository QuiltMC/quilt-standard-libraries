package org.quiltmc.qsl.chat.test;

import net.minecraft.text.Text;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.chat.api.client.ClientInboundSystemMessageEvents;
import org.quiltmc.qsl.chat.api.client.ClientOutboundChatMessageEvents;
import org.quiltmc.qsl.chat.api.server.ServerOutboundSystemMessageEvents;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.ArrayList;
import java.util.List;

public class ChatApiTest implements ModInitializer {
	private static final List<String> appends = new ArrayList<>();

	@Override
	public void onInitialize(ModContainer mod) {
		MixinEnvironment.getCurrentEnvironment().setOption(MixinEnvironment.Option.DEBUG_ALL, true);
		MixinEnvironment.getCurrentEnvironment().setOption(MixinEnvironment.Option.DEBUG_EXPORT, true);

		ClientOutboundChatMessageEvents.CANCEL.register(message -> {
			if (message.startsWith("!!append ")) {
				appends.add(message.replace("!!append ", ""));
				return true;
			} else {
				return false;
			}
		});

		ClientOutboundChatMessageEvents.CANCEL.register(message -> {
			if (message.startsWith("!!dump")) {
				appends.forEach(System.out::println);
				return true;
			} else {
				return false;
			}
		});

		ClientOutboundChatMessageEvents.MODIFY.register(message -> {
			if (appends.isEmpty()) {
				return message;
			} else {
				StringBuilder builder = new StringBuilder();
				builder.append(message);
				appends.forEach(builder::append);
				return builder.toString();
			}
		});

		ServerOutboundSystemMessageEvents.MODIFY.register(message -> message.copy().append(Text.literal(" owo")));
		ClientInboundSystemMessageEvents.MODIFY.register(wrapper -> wrapper.setContent(wrapper.getContext().copy().append(", uwu")));
	}
}
