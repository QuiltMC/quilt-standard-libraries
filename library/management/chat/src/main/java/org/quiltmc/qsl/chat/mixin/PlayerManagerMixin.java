package org.quiltmc.qsl.chat.mixin;

import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.OutgoingMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.quiltmc.qsl.chat.api.ChatEvents;
import org.quiltmc.qsl.chat.api.types.s2c.ImmutableSystemMessage;
import org.quiltmc.qsl.chat.api.types.s2c.MutableSystemMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
	@Redirect(method = "sendChatMessage(Lnet/minecraft/network/message/SignedChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;sendChatMessage(Lnet/minecraft/network/message/OutgoingMessage;ZLnet/minecraft/network/message/MessageType$Parameters;)V"))
	public void quilt$modifyAndCancelOutboundChatMessage(ServerPlayerEntity target, OutgoingMessage message, boolean filterMaskEnabled, MessageType.Parameters parameters) {

	}

	@Redirect(method = "broadcastSystemMessage(Lnet/minecraft/text/Text;Ljava/util/function/Function;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;sendSystemMessage(Lnet/minecraft/text/Text;Z)V"))
	public void quilt$modifyAndCancelOutboundSystemMessage(ServerPlayerEntity target, Text originalMessage, boolean overlay) {
		MutableSystemMessage message = new MutableSystemMessage(target.networkHandler, originalMessage, overlay);

		ChatEvents.MODIFY.invoke(message);

		ImmutableSystemMessage immutableMessage = message.immutableCopy();
		if (!ChatEvents.CANCEL.invoke(immutableMessage)) {
			target.sendSystemMessage(immutableMessage.getContent(), immutableMessage.isOverlay());
		}
	}
}
