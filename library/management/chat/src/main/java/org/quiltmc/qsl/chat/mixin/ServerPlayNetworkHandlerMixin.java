/*
 * Copyright 2023 The Quilt Project
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

import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.message.MessageType;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import org.quiltmc.qsl.chat.api.QuiltChatEvents;
import org.quiltmc.qsl.chat.api.types.ChatC2SMessage;
import org.quiltmc.qsl.chat.api.types.ChatS2CMessage;
import org.quiltmc.qsl.chat.api.types.ProfileIndependentS2CMessage;

@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
	@Shadow
	public ServerPlayerEntity player;

	@Unique
	private ProfileIndependentS2CMessage quilt$sendProfileIndependentMessage$storedProfileIndependentMessage;

	@ModifyVariable(method = "onChatMessage", at = @At("HEAD"), argsOnly = true)
	public ChatMessageC2SPacket quilt$modifyInboundChatMessage(ChatMessageC2SPacket packet) {
		var message = new ChatC2SMessage(this.player, false, packet);

		return ((ChatC2SMessage) QuiltChatEvents.MODIFY.invokeOrElse(message, message)).serialized();
	}

	@Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
	public void quilt$cancelInboundChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) {
		var message = new ChatC2SMessage(this.player, false, packet);

		if (QuiltChatEvents.CANCEL.invoke(message) == Boolean.TRUE) {
			QuiltChatEvents.CANCELLED.invoke(message);
			ci.cancel();
		}
	}

	/*
	 * Synthetic note: `method_44900(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;Ljava/util/Optional;)V`
	 * is the lambda passed to `this.server.submit` in `onChatMessage`
	 */
	@Inject(method = "method_44900(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;Ljava/util/Optional;)V", at = @At(value = "HEAD"))
	public void quilt$beforeInboundChatMessage(ChatMessageC2SPacket packet, Optional optional, CallbackInfo ci) {
		var immutableMessage = new ChatC2SMessage(this.player, false, packet);
		QuiltChatEvents.BEFORE_PROCESS.invoke(immutableMessage);
	}

	@Inject(method = "method_44900(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;Ljava/util/Optional;)V", at = @At(value = "RETURN"))
	public void quilt$afterInboundChatMessage(ChatMessageC2SPacket packet, Optional optional, CallbackInfo ci) {
		var immutableMessage = new ChatC2SMessage(this.player, false, packet);
		QuiltChatEvents.AFTER_PROCESS.invoke(immutableMessage);
	}

	@Inject(method = "sendProfileIndependentMessage", at = @At("HEAD"), cancellable = true)
	public void quilt$captureAndModifyAndCancelAndBeforeOutboundProfileIndependentMessage(Text message, MessageType.Parameters parameters, CallbackInfo ci) {
		var independentMessage = new ProfileIndependentS2CMessage(this.player, false, message, parameters);
		this.quilt$sendProfileIndependentMessage$storedProfileIndependentMessage = (ProfileIndependentS2CMessage) QuiltChatEvents.MODIFY.invokeOrElse(independentMessage, independentMessage);

		if (QuiltChatEvents.CANCEL.invoke(this.quilt$sendProfileIndependentMessage$storedProfileIndependentMessage) == Boolean.TRUE) {
			ci.cancel();
			return;
		}

		QuiltChatEvents.BEFORE_PROCESS.invoke(this.quilt$sendProfileIndependentMessage$storedProfileIndependentMessage);
	}

	@Redirect(
			method = "sendProfileIndependentMessage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"
			)
	)
	public void quilt$afterOutboundProfileIndependentMessage(ServerPlayNetworkHandler instance, Packet<?> packet) {
		instance.sendPacket(this.quilt$sendProfileIndependentMessage$storedProfileIndependentMessage.serialized());
		QuiltChatEvents.AFTER_PROCESS.invoke(this.quilt$sendProfileIndependentMessage$storedProfileIndependentMessage);
	}

	@Redirect(
			method = "sendChatMessage(Lnet/minecraft/network/message/SignedChatMessage;Lnet/minecraft/network/message/MessageType$Parameters;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"
			)
	)
	public void quilt$modifyAndCancelAndBeforeAndAfterOutboundChatMessage(ServerPlayNetworkHandler instance, Packet<?> packet) {
		if (packet instanceof ChatMessageS2CPacket chatMessageS2CPacket) {
			var message = new ChatS2CMessage(instance.player, false, chatMessageS2CPacket);
			message = (ChatS2CMessage) QuiltChatEvents.MODIFY.invokeOrElse(message, message);

			if (QuiltChatEvents.CANCEL.invoke(message) == Boolean.TRUE) {
				QuiltChatEvents.CANCELLED.invoke(message);
				return;
			}

			QuiltChatEvents.BEFORE_PROCESS.invoke(message);
			instance.sendPacket(message.serialized());
			QuiltChatEvents.AFTER_PROCESS.invoke(message);
		} else {
			throw new IllegalArgumentException("Received non-ChatMessageS2CPacket for argument to ServerPlayNetworkHandler.sendPacket in ServerPlayNetworkHandler.sendChatMessage");
		}
	}
}
