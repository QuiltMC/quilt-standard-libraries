/*
 * Copyright 2023 QuiltMC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.quiltmc.qsl.chat.mixin.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.ProfileIndependentMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.SystemMessageS2CPacket;
import org.objectweb.asm.Opcodes;
import org.quiltmc.qsl.chat.api.QuiltChatEvents;
import org.quiltmc.qsl.chat.api.types.ChatC2SMessage;
import org.quiltmc.qsl.chat.api.types.ChatS2CMessage;
import org.quiltmc.qsl.chat.api.types.ProfileIndependentS2CMessage;
import org.quiltmc.qsl.chat.api.types.RawChatC2SMessage;
import org.quiltmc.qsl.chat.api.types.SystemS2CMessage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@ModifyVariable(method = "onChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), argsOnly = true)
	public ChatMessageS2CPacket quilt$modifyInboundChatMessage(ChatMessageS2CPacket packet) {
		ChatS2CMessage message = new ChatS2CMessage(client.player, true, packet);
		return (ChatMessageS2CPacket)QuiltChatEvents.MODIFY.invoke(message, message).serialized();
	}

	@Inject(method = "onChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/ChatMessageS2CPacket;body()Lnet/minecraft/network/message/MessageBody$Serialized;", shift = At.Shift.BEFORE), cancellable = true)
	public void quilt$cancelInboundChatMessage(ChatMessageS2CPacket packet, CallbackInfo ci) {
		ChatS2CMessage message = new ChatS2CMessage(client.player, true, packet);
		if (QuiltChatEvents.CANCEL.invoke(message) == Boolean.TRUE) {
			ci.cancel();
		}
	}

	@Inject(method = "onChatMessage", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;client:Lnet/minecraft/client/MinecraftClient;", ordinal = 1, shift = At.Shift.BEFORE))
	public void quilt$beforeInboundChatMessage(ChatMessageS2CPacket packet, CallbackInfo ci) {
		ChatS2CMessage message = new ChatS2CMessage(client.player, true, packet);
		QuiltChatEvents.BEFORE_PROCESS.invoke(message);
	}

	@Inject(method = "onChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/message/MessageSignatureStorage;addMessageSignatures(Lnet/minecraft/network/message/SignedChatMessage;)V", shift = At.Shift.AFTER))
	public void quilt$afterInboundChatMessage(ChatMessageS2CPacket packet, CallbackInfo ci) {
		ChatS2CMessage message = new ChatS2CMessage(client.player, true, packet);
		QuiltChatEvents.AFTER_PROCESS.invoke(message);
	}

	@ModifyVariable(method = "onSystemMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), argsOnly = true)
	public SystemMessageS2CPacket quilt$modifyInboundSystemMessage(SystemMessageS2CPacket packet) {
		SystemS2CMessage message = new SystemS2CMessage(client.player, true, packet);
		return (SystemMessageS2CPacket)QuiltChatEvents.MODIFY.invoke(message, message).serialized();
	}

	@Inject(method = "onSystemMessage", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;client:Lnet/minecraft/client/MinecraftClient;", opcode = Opcodes.GETFIELD, ordinal = 1, shift = At.Shift.BEFORE), cancellable = true)
	public void quilt$cancelAndBeforeInboundSystemMessage(SystemMessageS2CPacket packet, CallbackInfo ci) {
		SystemS2CMessage message = new SystemS2CMessage(client.player, true, packet);
		if (QuiltChatEvents.CANCEL.invoke(message) == Boolean.TRUE) {
			ci.cancel();
		} else {
			QuiltChatEvents.BEFORE_PROCESS.invoke(message);
		}
	}

	@Inject(method = "onSystemMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ClientChatListener;m_tvzofpwk(Lnet/minecraft/text/Text;Z)V", shift = At.Shift.AFTER))
	public void quilt$afterInboundSystemMessage(SystemMessageS2CPacket packet, CallbackInfo ci) {
		SystemS2CMessage message = new SystemS2CMessage(client.player, true, packet);
		QuiltChatEvents.AFTER_PROCESS.invoke(message);
	}

	@ModifyVariable(method = "onProfileIndependentMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V", shift = At.Shift.AFTER), argsOnly = true)
	public ProfileIndependentMessageS2CPacket quilt$modifyInboundProfileIndependentMessage(ProfileIndependentMessageS2CPacket packet) {
		ProfileIndependentS2CMessage message = new ProfileIndependentS2CMessage(client.player, true, packet);
		return (ProfileIndependentMessageS2CPacket)QuiltChatEvents.MODIFY.invoke(message, message).serialized();
	}

	@Inject(method = "onProfileIndependentMessage", at = @At(value = "FIELD", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;client:Lnet/minecraft/client/MinecraftClient;", shift = At.Shift.BEFORE, ordinal = 1), cancellable = true)
	public void quilt$cancelInboundProfileIndependentMessage(ProfileIndependentMessageS2CPacket packet, CallbackInfo ci) {
		ProfileIndependentS2CMessage message = new ProfileIndependentS2CMessage(client.player, true, packet);
		if (QuiltChatEvents.CANCEL.invoke(message) == Boolean.TRUE) {
			ci.cancel();
		}
	}

	@Inject(method = "onProfileIndependentMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;m_xlacueca()Lnet/minecraft/client/gui/ClientChatListener;"))
	public void quilt$beforeInboundProfileIndependentMessage(ProfileIndependentMessageS2CPacket packet, CallbackInfo ci) {
		ProfileIndependentS2CMessage message = new ProfileIndependentS2CMessage(client.player, true, packet);
		QuiltChatEvents.BEFORE_PROCESS.invoke(message);
	}

	@Inject(method = "onProfileIndependentMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/ClientChatListener;m_jytgvbam(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageType$Parameters;)V", shift = At.Shift.AFTER))
	public void quilt$afterInboundProfileIndependentMessage(ProfileIndependentMessageS2CPacket packet, CallbackInfo ci) {
		ProfileIndependentS2CMessage message = new ProfileIndependentS2CMessage(client.player, true, packet);
		QuiltChatEvents.AFTER_PROCESS.invoke(message);
	}

	@ModifyVariable(method = "m_fzlgisyq", at = @At("HEAD"), argsOnly = true)
	public String quilt$modifyOutboundRawChatMessage(String string) {
		// Not sure *why* this would be null but IDEA is complaining, so, safety first?
		if (client.player == null) {
			return string;
		}

		RawChatC2SMessage message = new RawChatC2SMessage(client.player, true, string);
		return ((RawChatC2SMessage)QuiltChatEvents.MODIFY.invoke(message, message)).serialized();
	}

	@Inject(method = "m_fzlgisyq", at = @At(value = "HEAD"), cancellable = true)
	public void quilt$cancelAndBeforeOutboundRawChatMessage(String string, CallbackInfo ci) {
		if (client.player == null) {
			return;
		}

		RawChatC2SMessage message = new RawChatC2SMessage(client.player, true, string);
		if (QuiltChatEvents.CANCEL.invoke(message)) {
			ci.cancel();
		} else {
			QuiltChatEvents.BEFORE_PROCESS.invoke(message);
		}
	}

	@Inject(method = "m_fzlgisyq", at = @At(value = "TAIL"))
	public void quilt$afterOutboundRawChatMessage(String string, CallbackInfo ci) {
		if (client.player == null) {
			return;
		}

		RawChatC2SMessage message = new RawChatC2SMessage(client.player, true, string);
		QuiltChatEvents.AFTER_PROCESS.invoke(message);
	}

	@Redirect(method = "m_fzlgisyq", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
	public void quilt$modifyAndCancelAndBeforeAndAfterOutboundChatMessage(ClientPlayNetworkHandler instance, Packet<?> packet) {
		if (packet instanceof ChatMessageC2SPacket chatMessageC2SPacket) {
			ChatC2SMessage message = new ChatC2SMessage(client.player, true, chatMessageC2SPacket);
			message = (ChatC2SMessage)QuiltChatEvents.MODIFY.invoke(message, message);

			if (QuiltChatEvents.CANCEL.invoke(message) != Boolean.TRUE) {
				QuiltChatEvents.BEFORE_PROCESS.invoke(message);
				instance.sendPacket(message.serialized());
				QuiltChatEvents.AFTER_PROCESS.invoke(message);
			}
		} else {
			throw new IllegalArgumentException("Received non-ChatMessageC2SPacket for argument to ClientPlayNetworkHandler.sendPacket in ClientPlayNetworkHandler.m_fzlgisyq (sendChatMessage? mapping missing at time of writing)");
		}
	}
}
