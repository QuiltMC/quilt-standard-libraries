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

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import com.mojang.brigadier.ParseResults;
import org.quiltmc.qsl.chat.api.QuiltChatEvents;
import org.quiltmc.qsl.chat.api.types.ChatC2SMessage;
import org.quiltmc.qsl.chat.api.types.ChatS2CMessage;
import org.quiltmc.qsl.chat.api.types.CommandC2SMessage;
import org.quiltmc.qsl.chat.api.types.ProfileIndependentS2CMessage;
import org.quiltmc.qsl.chat.api.types.RemovalS2CMessage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.command.SignedArgument;
import net.minecraft.network.PacketSendListener;
import net.minecraft.network.message.MessageBody;
import net.minecraft.network.message.MessageChain;
import net.minecraft.network.message.MessageSignatureList;
import net.minecraft.network.message.MessageSignatureStorage;
import net.minecraft.network.message.MessageType;
import net.minecraft.network.message.SignedChatMessage;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.MessageRemovalS2CPacket;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
	@Shadow
	public ServerPlayerEntity player;

	@Shadow
	protected abstract Optional<MessageSignatureList> method_44337(
		String string, Instant instant, MessageSignatureList.Acknowledgment acknowledgment
	);

	@Shadow
	private MessageChain.Unpacker messageChainUnpacker;

	@Shadow
	protected abstract Map<String, SignedChatMessage> method_45006(
		ChatCommandC2SPacket commandPacket, SignedArgument<?> signedArgument, MessageSignatureList signatures
	) throws MessageChain.DecodingException;

	@Shadow
	protected abstract ParseResults<ServerCommandSource> method_45003(String string);

	@Shadow
	@Final
	private MessageSignatureStorage messageSignatureStorage;

	@Unique
	private ProfileIndependentS2CMessage quilt$sendProfileIndependentMessage$storedProfileIndependentMessage;

	@Unique
	private SignedChatMessage quilt$sendChatMessage$storedSignedChatMessage;

	//region Outbound Profile Independent Messages
	@Inject(method = "sendProfileIndependentMessage", at = @At("HEAD"), cancellable = true)
	public void quilt$captureAndModifyAndCancelAndBeforeOutboundProfileIndependentMessage(
		Text message, MessageType.Parameters parameters, CallbackInfo ci
	) {
		var independentMessage = new ProfileIndependentS2CMessage(player, false, message, parameters);
		quilt$sendProfileIndependentMessage$storedProfileIndependentMessage =
			(ProfileIndependentS2CMessage) QuiltChatEvents.MODIFY.invokeOrElse(independentMessage, independentMessage);

		if (QuiltChatEvents.CANCEL.invoke(quilt$sendProfileIndependentMessage$storedProfileIndependentMessage) == Boolean.TRUE) {
			ci.cancel();
			return;
		}

		QuiltChatEvents.BEFORE_PROCESS.invoke(quilt$sendProfileIndependentMessage$storedProfileIndependentMessage);
	}

	@Redirect(
		method = "sendProfileIndependentMessage",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"
		)
	)
	public void quilt$afterOutboundProfileIndependentMessage(ServerPlayNetworkHandler instance, Packet<?> packet) {
		instance.sendPacket(quilt$sendProfileIndependentMessage$storedProfileIndependentMessage.serialized());
		QuiltChatEvents.AFTER_PROCESS.invoke(quilt$sendProfileIndependentMessage$storedProfileIndependentMessage);
	}
	//endregion

	//region Inbound Chat Messages
	@Inject(
		method = "onChatMessage",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;message()Ljava/lang/String;"),
		cancellable = true
	)
	public void quilt$cancelInboundChatMessage(ChatMessageC2SPacket packet, CallbackInfo ci) throws MessageChain.DecodingException {
		var message = new ChatC2SMessage(player, false, packet);

		if (QuiltChatEvents.CANCEL.invoke(message) == Boolean.TRUE) {
			QuiltChatEvents.CANCELLED.invoke(message);
			ci.cancel();
			var result = method_44337(packet.message(), packet.timestamp(), packet.messageAcknowledgments());
			if (result.isPresent()) {
				MessageBody messageBody = new MessageBody(packet.message(), packet.timestamp(), packet.salt(), result.get());
				messageChainUnpacker.unpack(packet.signature(), messageBody);
			}
		}
	}

	@ModifyVariable(method = "onChatMessage", at = @At("HEAD"), argsOnly = true)
	public ChatMessageC2SPacket quilt$modifyInboundChatMessage(ChatMessageC2SPacket packet) {
		var message = new ChatC2SMessage(player, false, packet);

		return ((ChatC2SMessage) QuiltChatEvents.MODIFY.invokeOrElse(message, message)).serialized();
	}

	/*
	 * Synthetic note: `method_44900(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;Ljava/util/Optional;)V`
	 * is the lambda passed to `this.server.submit` in `onChatMessage`
	 */
	@Inject(
		method = "method_44900(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;Ljava/util/Optional;)V",
		at = @At(value = "HEAD")
	)
	public void quilt$beforeInboundChatMessage(ChatMessageC2SPacket packet, Optional optional, CallbackInfo ci) {
		var immutableMessage = new ChatC2SMessage(player, false, packet);
		QuiltChatEvents.BEFORE_PROCESS.invoke(immutableMessage);
	}

	@Inject(
		method = "method_44900(Lnet/minecraft/network/packet/c2s/play/ChatMessageC2SPacket;Ljava/util/Optional;)V",
		at = @At(value = "RETURN")
	)
	public void quilt$afterInboundChatMessage(ChatMessageC2SPacket packet, Optional optional, CallbackInfo ci) {
		var immutableMessage = new ChatC2SMessage(player, false, packet);
		QuiltChatEvents.AFTER_PROCESS.invoke(immutableMessage);
	}
	//endregion

	//region Outbound Chat Messages
	@Inject(
		method = "sendChatMessage(Lnet/minecraft/network/message/SignedChatMessage;Lnet/minecraft/network/message/MessageType$Parameters;)V",
		at = @At("HEAD")
	)
	public void quilt$captureOutboundSignedChatMessage(SignedChatMessage message, MessageType.Parameters parameters, CallbackInfo ci) {
		quilt$sendChatMessage$storedSignedChatMessage = message;
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

				// This is, technically, wrong!
				// We need the SignedChatMessage, but we cant construct one once we decompose it
				// Anyone messing with signatures should be able to fix this mod side though luckily
				// (and probably should even if it doesn't break, since cancellation is always after modification)
				// TODO: Maybe use some other mixin or expand the method calls manually to fix this?
				// - silver
				messageSignatureStorage.addMessageSignatures(quilt$sendChatMessage$storedSignedChatMessage);

				return;
			}

			QuiltChatEvents.BEFORE_PROCESS.invoke(message);
			instance.sendPacket(message.serialized());
			QuiltChatEvents.AFTER_PROCESS.invoke(message);
		} else {
			throw new IllegalArgumentException(
				"Received non-ChatMessageS2CPacket for argument to ServerPlayNetworkHandler.sendPacket in ServerPlayNetworkHandler.sendChatMessage"
			);
		}
	}
	//endregion

	//region Inbound Commands
	@ModifyVariable(method = "onChatCommand", at = @At("HEAD"), argsOnly = true)
	public ChatCommandC2SPacket quilt$modifyInboundCommand(ChatCommandC2SPacket packet) {
		var message = new CommandC2SMessage(player, false, packet);
		return (ChatCommandC2SPacket) QuiltChatEvents.MODIFY.invokeOrElse(message, message).serialized();
	}

	@Inject(
			method = "onChatCommand",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/packet/c2s/play/ChatCommandC2SPacket;command()Ljava/lang/String;"
			),
			cancellable = true
	)
	public void quilt$cancelInboundCommand(ChatCommandC2SPacket packet, CallbackInfo ci) throws MessageChain.DecodingException {
		var message = new CommandC2SMessage(player, false, packet);
		if (QuiltChatEvents.CANCEL.invoke(message) == Boolean.TRUE) {
			ci.cancel();
			QuiltChatEvents.CANCELLED.invoke(message);
			var result = method_44337(packet.command(), packet.timestamp(), packet.messageAcknowledgements());
			if (result.isPresent()) {
				var parseResults = method_45003(packet.command());
				method_45006(packet, SignedArgument.method_45043(parseResults), result.get());
			}
		}
	}

	/*
	 * Synthetic note: `method_44356(Lnet/minecraft/network/packet/c2s/play/ChatCommandC2SPacket;Ljava/util/Optional;)V`
	 * is the lambda passed to `this.server.submit` in `onChatCommand`
	 */
	@Inject(
			method = "method_44356(Lnet/minecraft/network/packet/c2s/play/ChatCommandC2SPacket;Ljava/util/Optional;)V",
			at = @At("HEAD")
	)
	public void quilt$beforeInboundCommand(ChatCommandC2SPacket packet, Optional<MessageSignatureList> optional, CallbackInfo ci) {
		var message = new CommandC2SMessage(player, false, packet);
		QuiltChatEvents.BEFORE_PROCESS.invoke(message);
	}

	@Inject(
			method = "method_44356(Lnet/minecraft/network/packet/c2s/play/ChatCommandC2SPacket;Ljava/util/Optional;)V",
			at = @At("TAIL")
	)
	public void quilt$afterInboundCommand(ChatCommandC2SPacket packet, Optional<MessageSignatureList> optional, CallbackInfo ci) {
		var message = new CommandC2SMessage(player, false, packet);
		QuiltChatEvents.AFTER_PROCESS.invoke(message);
	}
	//endregion

	//region Outbound Message Removal
	@ModifyVariable(
		method = "sendPacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketSendListener;)V",
		at = @At("HEAD"),
		argsOnly = true
	)
	public Packet<?> quilt$modifyOutboundMessageRemoval(Packet<?> packet) {
		if (packet instanceof MessageRemovalS2CPacket removalPacket) {
			var message = new RemovalS2CMessage(player, false, removalPacket);
			return (Packet<?>) QuiltChatEvents.MODIFY.invokeOrElse(message, message).serialized();
		} else {
			return packet;
		}
	}

	@Inject(
		method = "sendPacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketSendListener;)V",
		at = @At(
			value = "FIELD",
			target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;connection:Lnet/minecraft/network/ClientConnection;"
		),
		cancellable = true
	)
	public void quilt$cancelAndBeforeOutboundMessageRemoval(Packet<?> packet, PacketSendListener listener, CallbackInfo ci) {
		if (packet instanceof MessageRemovalS2CPacket removalPacket) {
			var message = new RemovalS2CMessage(player, false, removalPacket);
			if (QuiltChatEvents.CANCEL.invoke(message) == Boolean.TRUE) {
				QuiltChatEvents.CANCELLED.invoke(message);
				ci.cancel();
			} else {
				QuiltChatEvents.BEFORE_PROCESS.invoke(message);
			}
		}
	}

	@Inject(
		method = "sendPacket(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketSendListener;)V",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/ClientConnection;send(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/PacketSendListener;)V",
			shift = At.Shift.AFTER
		)
	)
	public void quilt$afterOutboundMessageRemoval(Packet<?> packet, PacketSendListener listener, CallbackInfo ci) {
		if (packet instanceof MessageRemovalS2CPacket removalPacket) {
			var message = new RemovalS2CMessage(player, false, removalPacket);
			QuiltChatEvents.AFTER_PROCESS.invoke(message);
		}
	}
	//endregion
}
