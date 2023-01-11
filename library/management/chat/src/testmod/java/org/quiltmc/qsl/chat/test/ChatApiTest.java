package org.quiltmc.qsl.chat.test;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.chat.api.QuiltChatEvents;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public class ChatApiTest implements ModInitializer {
	private static final List<String> appends = new ArrayList<>();

	@Override
	public void onInitialize(ModContainer mod) {
		MixinEnvironment.getCurrentEnvironment().setOption(MixinEnvironment.Option.DEBUG_ALL, true);
		MixinEnvironment.getCurrentEnvironment().setOption(MixinEnvironment.Option.DEBUG_EXPORT, true);

		QuiltChatEvents.AFTER_IO.register(EnumSet.allOf(QuiltMessageType.class), System.out::println);
	}
}
