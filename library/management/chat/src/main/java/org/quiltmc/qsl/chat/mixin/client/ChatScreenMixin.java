package org.quiltmc.qsl.chat.mixin.client;

import net.minecraft.client.gui.screen.ChatScreen;
import org.objectweb.asm.Opcodes;
import org.quiltmc.qsl.chat.api.client.ClientOutboundChatMessageEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// If we don't inject into the screen, the client sees the original message even if it was modified!
// McDev hates this injection for some reason though, works fine anyways!
// Offsets are weird but it makes it work somehow
// - Silver
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
		return ClientOutboundChatMessageEvents.MODIFY.invoker().beforeChatMessageSent(message);
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
		if (ClientOutboundChatMessageEvents.CANCEL.invoker().cancelChatMessage(text)) {
			// yes we totally sent that, trust trust
			cir.setReturnValue(true);
		}
	}
}
