package org.quiltmc.qsl.chat.mixin.client;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.SystemMessageS2CPacket;
import org.quiltmc.qsl.chat.api.client.ClientInboundChatMessageEvents;
import org.quiltmc.qsl.chat.api.client.ClientInboundSystemMessageEvents;
import org.quiltmc.qsl.chat.impl.client.ChatMessageWrapper;
import org.quiltmc.qsl.chat.impl.client.SystemMessageWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@ModifyVariable(method = "onChatMessage", at = @At("HEAD"), argsOnly = true)
	public ChatMessageS2CPacket quilt$modifyInboundChatMessage(ChatMessageS2CPacket packet) {
		ChatMessageWrapper wrapper = new ChatMessageWrapper(packet);
		ClientInboundChatMessageEvents.MODIFY.invoker().modifyReceivedChatMessage(wrapper);
		return wrapper.asPacket();
	}

	@Inject(method = "onChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V"), cancellable = true)
	public void quilt$cancelReceiveInboundChatMessage(ChatMessageS2CPacket packet, CallbackInfo ci) {
		ChatMessageWrapper wrapper = new ChatMessageWrapper(packet);
		if (ClientInboundChatMessageEvents.CANCEL.invoker().cancelChatMessage(wrapper)) {
			ci.cancel();
		}
	}

	@ModifyVariable(method = "onSystemMessage", at = @At("HEAD"), argsOnly = true)
	public SystemMessageS2CPacket quilt$modifyInboundSystemMessage(SystemMessageS2CPacket packet) {
		SystemMessageWrapper wrapper = new SystemMessageWrapper(packet);
		ClientInboundSystemMessageEvents.MODIFY.invoker().modifyReceivedSystemMessage(wrapper);
		return wrapper.asPacket();
	}

	@Inject(method = "onSystemMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V"), cancellable = true)
	public void quilt$cancelReceiveInboundChatMessage(SystemMessageS2CPacket packet, CallbackInfo ci) {
		SystemMessageWrapper wrapper = new SystemMessageWrapper(packet);
		if (ClientInboundSystemMessageEvents.CANCEL.invoker().cancelSystemMessage(wrapper)) {
			ci.cancel();
		}
	}
}
