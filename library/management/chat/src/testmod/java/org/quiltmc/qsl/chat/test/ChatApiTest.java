package org.quiltmc.qsl.chat.test;

import net.minecraft.text.Text;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.chat.api.QuiltChatEvents;
import org.quiltmc.qsl.chat.api.QuiltMessageType;
import org.quiltmc.qsl.chat.api.types.MutableS2CSystemMessage;
import org.spongepowered.asm.mixin.MixinEnvironment;

import java.util.EnumSet;
import java.util.Random;

public class ChatApiTest implements ModInitializer {
	@Override
	public void onInitialize(ModContainer mod) {
		MixinEnvironment.getCurrentEnvironment().setOption(MixinEnvironment.Option.DEBUG_ALL, true);
		MixinEnvironment.getCurrentEnvironment().setOption(MixinEnvironment.Option.DEBUG_EXPORT, true);

		QuiltChatEvents.AFTER_IO.register(EnumSet.allOf(QuiltMessageType.class), System.out::println);

		QuiltChatEvents.MODIFY.register(EnumSet.of(QuiltMessageType.SYSTEM, QuiltMessageType.SERVER, QuiltMessageType.OUTBOUND), abstractMessage -> {
			if (abstractMessage instanceof MutableS2CSystemMessage mutableS2CSystemMessage) {
				Text content = mutableS2CSystemMessage.getContent();
				if (new Random().nextBoolean()) {
					mutableS2CSystemMessage.setContent(content.copy().append(Text.literal(", uwu")));
				}
			}
		});
	}
}
