package org.quiltmc.qsl.chat.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ProfileIndependentMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.SystemMessageS2CPacket;
import org.objectweb.asm.Opcodes;
import org.quiltmc.qsl.chat.api.ChatEvents;
import org.quiltmc.qsl.chat.api.client.ClientInboundChatMessageEvents;
import org.quiltmc.qsl.chat.api.client.ClientInboundSystemMessageEvents;
import org.quiltmc.qsl.chat.api.types.s2c.ImmutableProfileIndependentMessage;
import org.quiltmc.qsl.chat.api.types.s2c.MutableProfileIndependentMessage;
import org.quiltmc.qsl.chat.impl.client.ChatMessageWrapper;
import org.quiltmc.qsl.chat.impl.client.SystemMessageWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@ModifyVariable(method = "onChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), argsOnly = true)
	public ChatMessageS2CPacket quilt$modifyInboundChatMessage(ChatMessageS2CPacket packet) {
		ChatMessageWrapper wrapper = new ChatMessageWrapper(packet);
		ClientInboundChatMessageEvents.MODIFY.invoker().onReceivedChatMessage(wrapper);
		return wrapper.asPacket();
	}

	@Inject(method = "onChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/ChatMessageS2CPacket;body()Lnet/minecraft/network/message/MessageBody$Serialized;", shift = At.Shift.BEFORE), cancellable = true)
	public void quilt$cancelInboundChatMessage(ChatMessageS2CPacket packet, CallbackInfo ci) {
		ChatMessageWrapper wrapper = new ChatMessageWrapper(packet);
		if (ClientInboundChatMessageEvents.CANCEL.invoker().cancelChatMessage(wrapper)) {
			ci.cancel();
		}
	}

	@ModifyVariable(method = "onSystemMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), argsOnly = true)
	public SystemMessageS2CPacket quilt$modifyInboundSystemMessage(SystemMessageS2CPacket packet) {
		SystemMessageWrapper wrapper = new SystemMessageWrapper(packet);
		ClientInboundSystemMessageEvents.MODIFY.invoker().onReceivedSystemMessage(wrapper);
		return wrapper.asPacket();
	}

	@Inject(method = "onSystemMessage", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;client:Lnet/minecraft/client/MinecraftClient;", opcode = Opcodes.GETFIELD, ordinal = 1, shift = At.Shift.BEFORE), cancellable = true)
	public void quilt$cancelInboundSystemMessage(SystemMessageS2CPacket packet, CallbackInfo ci) {
		SystemMessageWrapper wrapper = new SystemMessageWrapper(packet);
		if (ClientInboundSystemMessageEvents.CANCEL.invoker().cancelSystemMessage(wrapper)) {
			ci.cancel();
		}
	}

	@ModifyVariable(method = "onProfileIndependentMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), argsOnly = true)
	public ProfileIndependentMessageS2CPacket quilt$modifyInboundProfileIndependentMessage(ProfileIndependentMessageS2CPacket packet) {
		MutableProfileIndependentMessage message = new MutableProfileIndependentMessage(client.player, packet);
		ChatEvents.MODIFY.invoke(message);
		return message.packet();
	}

	@Inject(method = "onProfileIndependentMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/ProfileIndependentMessageS2CPacket;message()Lnet/minecraft/text/Text;", shift = At.Shift.BEFORE), cancellable = true)
	public void quilt$cancelInboundProfileIndependentMessage(ProfileIndependentMessageS2CPacket packet, CallbackInfo ci) {
		ImmutableProfileIndependentMessage message = new ImmutableProfileIndependentMessage(client.player, packet);
		if (ChatEvents.CANCEL.invoke(message)) {
			ci.cancel();
		}
	}
}
