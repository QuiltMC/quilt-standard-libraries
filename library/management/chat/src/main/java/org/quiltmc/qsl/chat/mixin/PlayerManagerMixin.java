package org.quiltmc.qsl.chat.mixin;

import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.OutgoingMessage;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.quiltmc.qsl.chat.api.server.ServerOutboundChatMessageEvents;
import org.quiltmc.qsl.chat.api.server.ServerOutboundSystemMessageEvents;
import org.quiltmc.qsl.chat.impl.server.SendMessageWrapper;
import org.quiltmc.qsl.chat.impl.server.SystemMessageWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PlayerManager.class)
public class PlayerManagerMixin {
	@Redirect(method = "sendChatMessage(Lnet/minecraft/network/message/SignedChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;sendChatMessage(Lnet/minecraft/network/message/OutgoingMessage;ZLnet/minecraft/network/message/MessageType$Parameters;)V"))
	public void quilt$cancelAndModifyOutboundChatMessage(ServerPlayerEntity target, OutgoingMessage message, boolean filterMaskEnabled, MessageType.Parameters parameters) {
		SendMessageWrapper wrapper = new SendMessageWrapper(target, message, filterMaskEnabled, parameters);
		ServerOutboundChatMessageEvents.MODIFY.invoker().beforeChatMessageSent(wrapper);

		if (!ServerOutboundChatMessageEvents.CANCEL.invoker().cancelChatMessage(target, wrapper.getMessage(), wrapper.isFilterMaskEnabled(), wrapper.getParameters())) {
			target.sendChatMessage(wrapper.getMessage(), wrapper.isFilterMaskEnabled(), wrapper.getParameters());
		}
	}

	@Redirect(method = "sendChatMessage(Lnet/minecraft/network/message/SignedChatMessage;Ljava/util/function/Predicate;Lnet/minecraft/server/network/ServerPlayerEntity;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;sendSystemMessage(Lnet/minecraft/text/Text;)V"))
	public void quilt$cancelAndModifyOutboundSystemMessage(ServerPlayerEntity target, Text originalMessage) {
		SystemMessageWrapper wrapper = new SystemMessageWrapper(target, originalMessage, false);
		ServerOutboundSystemMessageEvents.MODIFY.invoker().beforeSystemMessageSent(wrapper);

		if (!ServerOutboundSystemMessageEvents.CANCEL.invoker().cancelSystemMessage(wrapper)) {
			target.sendSystemMessage(wrapper.getContext(), wrapper.isOverlay());
		}
	}

	@Redirect(method = "broadcastSystemMessage(Lnet/minecraft/text/Text;Ljava/util/function/Function;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayerEntity;sendSystemMessage(Lnet/minecraft/text/Text;Z)V"))
	public void quilt$cancelAndModifyOutboundSystemMessage(ServerPlayerEntity target, Text originalMessage, boolean overlay) {
		SystemMessageWrapper wrapper = new SystemMessageWrapper(target, originalMessage, false);
		ServerOutboundSystemMessageEvents.MODIFY.invoker().beforeSystemMessageSent(wrapper);

		if (!ServerOutboundSystemMessageEvents.CANCEL.invoker().cancelSystemMessage(wrapper)) {
			target.sendSystemMessage(wrapper.getContext(), wrapper.isOverlay());
		}
	}
}
