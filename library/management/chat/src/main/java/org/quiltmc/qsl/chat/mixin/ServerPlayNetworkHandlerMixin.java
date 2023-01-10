package org.quiltmc.qsl.chat.mixin;

import net.minecraft.network.Packet;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.quiltmc.qsl.chat.api.QuiltChatEvents;
import org.quiltmc.qsl.chat.api.types.c2s.ImmutableC2SChatMessage;
import org.quiltmc.qsl.chat.api.types.s2c.ImmutableS2CChatMessage;
import org.quiltmc.qsl.chat.api.types.s2c.ImmutableS2CProfileIndependentMessage;
import org.quiltmc.qsl.chat.api.types.c2s.MutableC2SChatMessage;
import org.quiltmc.qsl.chat.api.types.s2c.MutableS2CChatMessage;
import org.quiltmc.qsl.chat.api.types.s2c.MutableS2CProfileIndependentMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
	@Shadow
	public ServerPlayerEntity player;

	@Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
	public void quilt$modifyAndCancelInboundChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
		MutableC2SChatMessage message = new MutableC2SChatMessage(player, packet);

		QuiltChatEvents.MODIFY.invoke(message);

		ImmutableC2SChatMessage immutableMessage = message.immutableCopy();
		if (QuiltChatEvents.CANCEL.invoke(immutableMessage) == Boolean.TRUE) {
			ci.cancel();
		}
	}

	@Inject(method = "sendProfileIndependentMessage", at = @At("HEAD"), cancellable = true)
	public void quilt$modifyAndCancelOutboundProfileIndependentMessage(Text message, MessageType.Parameters parameters, CallbackInfo ci) {
		MutableS2CProfileIndependentMessage independentMessage = new MutableS2CProfileIndependentMessage(player, message, parameters);

		QuiltChatEvents.MODIFY.invoke(independentMessage);

		ImmutableS2CProfileIndependentMessage immutableIndependentMessage = independentMessage.immutableCopy();
		if (QuiltChatEvents.CANCEL.invoke(immutableIndependentMessage) == Boolean.TRUE) {
			ci.cancel();
		}
	}

	@Redirect(method = "sendChatMessage(Lnet/minecraft/network/message/SignedChatMessage;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
	public void quilt$modifyAndCancelOutboundChatMessage(ServerPlayNetworkHandler instance, Packet<?> packet) {
		if (packet instanceof ChatMessageS2CPacket chatMessageS2CPacket) {
			MutableS2CChatMessage message = new MutableS2CChatMessage(instance.player, false, chatMessageS2CPacket);
			QuiltChatEvents.MODIFY.invoke(message);

			ImmutableS2CChatMessage immutableMessage = message.immutableCopy();
			if (QuiltChatEvents.CANCEL.invoke(immutableMessage) == Boolean.TRUE) {
				return;
			}
			QuiltChatEvents.BEFORE_IO.invoke(immutableMessage);
			instance.sendPacket(immutableMessage.asPacket());
			QuiltChatEvents.AFTER_IO.invoke(immutableMessage);
		} else {
			throw new IllegalArgumentException("Received non-ChatMessageS2CPacket for argument to ServerPlayNetworkHandler.sendPacket in ServerPlayNetworkHandler.sendChatMessage");
		}
	}
}
