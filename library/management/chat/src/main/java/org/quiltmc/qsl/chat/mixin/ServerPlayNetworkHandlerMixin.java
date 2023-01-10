package org.quiltmc.qsl.chat.mixin;

import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.quiltmc.qsl.chat.api.ChatEvents;
import org.quiltmc.qsl.chat.api.types.s2c.ImmutableChatMessage;
import org.quiltmc.qsl.chat.api.types.s2c.ImmutableProfileIndependentMessage;
import org.quiltmc.qsl.chat.api.types.s2c.MutableChatMessage;
import org.quiltmc.qsl.chat.api.types.s2c.MutableProfileIndependentMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
	@Shadow
	public ServerPlayerEntity player;

	@Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
	public void quilt$modifyAndCancelInboundChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
		MutableChatMessage message = new MutableChatMessage((ServerPlayNetworkHandler)(Object)this, packet);

		ChatEvents.MODIFY.invoke(message);

		ImmutableChatMessage immutableMessage = message.immutableCopy();
		if (ChatEvents.CANCEL.invoke(immutableMessage)) {
			ci.cancel();
		}
	}

	@Inject(method = "sendProfileIndependentMessage", at = @At("HEAD"), cancellable = true)
	public void quilt$modifyAndCancelOutboundProfileIndependentMessage(Text message, MessageType.Parameters parameters, CallbackInfo ci) {
		MutableProfileIndependentMessage independentMessage = new MutableProfileIndependentMessage((player, message, parameters);

		ChatEvents.MODIFY.invoke(independentMessage);

		ImmutableProfileIndependentMessage immutableIndependentMessage = independentMessage.immutableCopy();
		if (ChatEvents.CANCEL.invoke(immutableIndependentMessage)) {
			ci.cancel();
		}
	}
}
