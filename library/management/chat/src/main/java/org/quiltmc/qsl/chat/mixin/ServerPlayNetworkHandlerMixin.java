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

package org.quiltmc.qsl.chat.mixin;

import net.minecraft.network.Packet;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.quiltmc.qsl.chat.api.QuiltChatEvents;
import org.quiltmc.qsl.chat.api.types.ChatC2SMessage;
import org.quiltmc.qsl.chat.api.types.ChatS2CMessage;
import org.quiltmc.qsl.chat.api.types.ProfileIndependentS2CMessage;
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
		ChatC2SMessage message = new ChatC2SMessage(player, false, packet);

		message = (ChatC2SMessage)QuiltChatEvents.MODIFY.invoke(message, message);

		if (QuiltChatEvents.CANCEL.invoke(message) == Boolean.TRUE) {
			ci.cancel();
		}
	}

	@Inject(method = "onChatMessage", at = @At(value = "FIELD", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;server:Lnet/minecraft/server/MinecraftServer;"))
	public void quilt$beforeInboundChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
		ChatC2SMessage immutableMessage = new ChatC2SMessage(player, false, packet);
		QuiltChatEvents.BEFORE_IO.invoke(immutableMessage);
	}

	@Inject(method = "onChatMessage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/MinecraftServer;submit(Ljava/lang/Runnable;)Ljava/util/concurrent/CompletableFuture;", shift = At.Shift.AFTER))
	public void quilt$afterInboundChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
		ChatC2SMessage immutableMessage = new ChatC2SMessage(player, false, packet);
		QuiltChatEvents.AFTER_IO.invoke(immutableMessage);
	}

	@Inject(method = "sendProfileIndependentMessage", at = @At("HEAD"), cancellable = true)
	public void quilt$modifyAndCancelAndBeforeOutboundProfileIndependentMessage(Text message, MessageType.Parameters parameters, CallbackInfo ci) {
		ProfileIndependentS2CMessage independentMessage = new ProfileIndependentS2CMessage(player, false, message, parameters);

		independentMessage = (ProfileIndependentS2CMessage)QuiltChatEvents.MODIFY.invoke(independentMessage, independentMessage);

		if (QuiltChatEvents.CANCEL.invoke(independentMessage) == Boolean.TRUE) {
			ci.cancel();
		} else {
			QuiltChatEvents.BEFORE_IO.invoke(independentMessage);
		}
	}

	@Inject(method = "sendProfileIndependentMessage", at = @At("TAIL"))
	public void quilt$afterProfileIndependentMessage(Text message, MessageType.Parameters parameters, CallbackInfo ci) {
		ProfileIndependentS2CMessage immutableMessage = new ProfileIndependentS2CMessage(player, false, message, parameters);
		QuiltChatEvents.AFTER_IO.invoke(immutableMessage);
	}

	@Redirect(method = "sendChatMessage(Lnet/minecraft/network/message/SignedChatMessage;Lnet/minecraft/network/message/MessageType$Parameters;)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;)V"))
	public void quilt$modifyAndCancelOutboundChatMessage(ServerPlayNetworkHandler instance, Packet<?> packet) {
		if (packet instanceof ChatMessageS2CPacket chatMessageS2CPacket) {
			ChatS2CMessage message = new ChatS2CMessage(instance.player, false, chatMessageS2CPacket);
			message = (ChatS2CMessage)QuiltChatEvents.MODIFY.invoke(message, message);

			if (QuiltChatEvents.CANCEL.invoke(message) == Boolean.TRUE) {
				return;
			}
			QuiltChatEvents.BEFORE_IO.invoke(message);
			instance.sendPacket(message.asPacket());
			QuiltChatEvents.AFTER_IO.invoke(message);
		} else {
			throw new IllegalArgumentException("Received non-ChatMessageS2CPacket for argument to ServerPlayNetworkHandler.sendPacket in ServerPlayNetworkHandler.sendChatMessage");
		}
	}
}
