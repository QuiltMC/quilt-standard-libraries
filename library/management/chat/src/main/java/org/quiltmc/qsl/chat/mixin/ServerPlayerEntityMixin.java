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

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.PacketSendListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import org.quiltmc.qsl.chat.api.QuiltChatEvents;
import org.quiltmc.qsl.chat.api.types.SystemS2CMessage;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
	private SystemS2CMessage quilt$sendSystemMessage$storedSystemMessage;

	@Inject(method = "sendSystemMessage(Lnet/minecraft/text/Text;Z)V", at = @At("HEAD"))
	public void quilt$captureAndModifyOutboundSystemMessage(Text originalMessage, boolean overlay, CallbackInfo ci) {
		var message = new SystemS2CMessage((ServerPlayerEntity) (Object) this, false, originalMessage, overlay);

		this.quilt$sendSystemMessage$storedSystemMessage = (SystemS2CMessage) QuiltChatEvents.MODIFY.invokeOrElse(message, message);
	}

	@Redirect(
			method = "sendSystemMessage(Lnet/minecraft/text/Text;Z)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketSendListener;)V"
			)
	)
	public void quilt$cancelAndBeforeAndAfterOutboundSystemMessage(ServerPlayNetworkHandler instance, Packet<?> packet, PacketSendListener listener) {
		if (QuiltChatEvents.CANCEL.invoke(this.quilt$sendSystemMessage$storedSystemMessage) != Boolean.TRUE) {
			QuiltChatEvents.BEFORE_PROCESS.invoke(this.quilt$sendSystemMessage$storedSystemMessage);
			instance.sendPacket(this.quilt$sendSystemMessage$storedSystemMessage.serialized(), listener);
			QuiltChatEvents.AFTER_PROCESS.invoke(this.quilt$sendSystemMessage$storedSystemMessage);
		} else {
			QuiltChatEvents.CANCELLED.invoke(this.quilt$sendSystemMessage$storedSystemMessage);
		}
	}
}
