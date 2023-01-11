package org.quiltmc.qsl.chat.mixin.client;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.objectweb.asm.Opcodes;
import org.quiltmc.qsl.chat.api.QuiltChatEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * We inject "outbound chat message" here instead of in {@link ClientPlayNetworkHandler} because this is also where messages are added to chat history.
 * So if we didnt add our events here, than users would end up petty confused, and mods would have to patch it themselves.
 * If a mod is sending chat messages for a player, its up to them to call and use these events (same with block break events).
 * <p>
 * Mixin injection points are kind of cursed but it worked fine for me,
 * might be worth making an automated test to detect if {@link QuiltChatEvents#CANCEL} is invoked before {@link QuiltChatEvents#MODIFY}?
 * @author Silver
 */
@Mixin(ChatScreen.class)
public class ChatScreenMixin {
	@ModifyVariable(method = "handleChatInput",
			at = @At(
					value = "FIELD",
					opcode = Opcodes.GETFIELD,
					ordinal = 0,
					shift = At.Shift.BY,
					by = -5
			), argsOnly = true, ordinal = 0)
	public String quilt$modifyOutboundChatMessage(String message) {
		return message;
	}

	@Inject(method = "handleChatInput",
			at = @At(
					value = "FIELD",
					opcode = Opcodes.GETFIELD,
					ordinal = 0,
					shift = At.Shift.BY,
					by = -4
			),
			cancellable = true
	)
	public void quilt$cancelOutboundChatMessage(String text, boolean addToHistory, CallbackInfoReturnable<Boolean> cir) {

	}
}
