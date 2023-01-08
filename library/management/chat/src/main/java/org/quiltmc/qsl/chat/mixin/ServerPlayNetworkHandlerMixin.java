package org.quiltmc.qsl.chat.mixin;

import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.quiltmc.qsl.chat.api.server.ServerInboundChatMessageEvents;
import org.quiltmc.qsl.chat.impl.server.ChatMessageWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
	@ModifyVariable(method = "onChatMessage", at = @At("HEAD"), argsOnly = true)
	public ChatMessageC2SPacket quilt$modifyInboundChatMessage(ChatMessageC2SPacket packet) {
		ChatMessageWrapper wrapper = new ChatMessageWrapper(packet);
		ServerInboundChatMessageEvents.MODIFY.invoker().modifyReceivedChatMessage(wrapper);
		return wrapper.asPacket();
	}

	@Inject(method = "onChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;hasInvalidCharacters(Ljava/lang/String;)Z"), cancellable = true)
	public void quilt$cancelReceiveInboundChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
		ChatMessageWrapper wrapper = new ChatMessageWrapper(packet);
		if (ServerInboundChatMessageEvents.CANCEL.invoker().cancelChatMessage(wrapper)) {
			ci.cancel();
		}
	}
}
