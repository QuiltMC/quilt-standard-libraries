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
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.registry.ClientRegistryLayer;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.encryption.PublicChatSession;
import net.minecraft.network.message.*;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.ChatCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.ChatMessageC2SPacket;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.MessageRemovalS2CPacket;
import net.minecraft.network.packet.s2c.play.ProfileIndependentMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.SystemMessageS2CPacket;
import net.minecraft.registry.LayeredRegistryManager;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.quiltmc.qsl.chat.api.QuiltChatEvents;
import org.quiltmc.qsl.chat.api.types.*;
import org.quiltmc.qsl.chat.impl.MessageChainReverseLookup;
import org.quiltmc.qsl.chat.impl.mixin.ChatSecurityRollbackSupport;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
	@Shadow
	@Final
	private MinecraftClient client;

	@Shadow
	public abstract @Nullable PlayerListEntry getPlayerListEntry(UUID uuid);

	@Shadow
	@Final
	private ClientConnection connection;

	@Shadow
	@Final
	private static Text CHAT_VALIDATION_FAILED_DISCONNECT;

	@Shadow
	private MessageSignatureStorage messageSignatureStorage;

	@Shadow
	private LayeredRegistryManager<ClientRegistryLayer> clientRegistryManager;

	@Shadow
	@Final
	private static Text INVALID_PACKET_DISCONNECT;

	@Shadow
	private LastSeenMessageTracker lastSeenMessageTracker;

	@Shadow
	private MessageChain.Packer messageChainPacker;

	@ModifyVariable(
			method = "onChatMessage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
					shift = At.Shift.AFTER
			),
			argsOnly = true
	)
	public ChatMessageS2CPacket quilt$modifyInboundChatMessage(ChatMessageS2CPacket packet) {
		var message = new ChatS2CMessage(client.player, true, packet);
		return (ChatMessageS2CPacket) QuiltChatEvents.MODIFY.invokeOrElse(message, message).serialized();
	}

	@Inject(
			method = "onChatMessage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/packet/s2c/play/ChatMessageS2CPacket;body()Lnet/minecraft/network/message/MessageBody$Serialized;",
					shift = At.Shift.BEFORE
			),
			cancellable = true
	)
	public void quilt$cancelInboundChatMessage(ChatMessageS2CPacket packet, CallbackInfo ci) {
		var message = new ChatS2CMessage(client.player, true, packet);
		if (QuiltChatEvents.CANCEL.invoke(message) == Boolean.TRUE) {
			QuiltChatEvents.CANCELLED.invoke(message);
			ci.cancel();
			UUID uuid = message.serialized().sender();
			var playerListEntry = getPlayerListEntry(uuid);
			if (playerListEntry == null) {
				connection.disconnect(CHAT_VALIDATION_FAILED_DISCONNECT);
			} else {
				// This is a LOT but we need to extract and track the signatures
				Optional<MessageBody> body = packet.body().createBody(messageSignatureStorage);
				Optional<MessageType.Parameters> parameters = packet.messageType()
					.createParameters(clientRegistryManager.getCompositeManager());
				if (body.isPresent() && parameters.isPresent()) {
					PublicChatSession publicChatSession = playerListEntry.getChatSession();
					MessageLink messageLink;
					if (publicChatSession != null) {
						messageLink = new MessageLink(packet.index(), uuid, publicChatSession.sessionId());
					} else {
						messageLink = MessageLink.create(uuid);
					}

					SignedChatMessage signedChatMessage = new SignedChatMessage(
							messageLink, packet.signature(), body.get(), packet.unsignedContent(), packet.filterMask()
					);
					if (!playerListEntry.getMessageVerifier().updateAndValidate(signedChatMessage)) {
						connection.disconnect(CHAT_VALIDATION_FAILED_DISCONNECT);
					} else {
						((ClientChatListenerInvoker) client.getChatListener()).invokeHandleMessage(signedChatMessage.signature(), () -> {
							var clientPlayNetworkHandler = client.getNetworkHandler();
							if (clientPlayNetworkHandler != null) {
								clientPlayNetworkHandler.acknowledge(signedChatMessage, true);
							}

							return false;
						});
						messageSignatureStorage.addMessageSignatures(signedChatMessage);
					}
				} else {
					connection.disconnect(INVALID_PACKET_DISCONNECT);
				}
			}
		}
	}

	@Inject(
			method = "onChatMessage",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;client:Lnet/minecraft/client/MinecraftClient;",
					ordinal = 1,
					shift = At.Shift.BEFORE
			)
	)
	public void quilt$beforeInboundChatMessage(ChatMessageS2CPacket packet, CallbackInfo ci) {
		var message = new ChatS2CMessage(client.player, true, packet);
		QuiltChatEvents.BEFORE_PROCESS.invoke(message);
	}

	@Inject(
			method = "onChatMessage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/message/MessageSignatureStorage;addMessageSignatures(Lnet/minecraft/network/message/SignedChatMessage;)V",
					shift = At.Shift.AFTER
			)
	)
	public void quilt$afterInboundChatMessage(ChatMessageS2CPacket packet, CallbackInfo ci) {
		var message = new ChatS2CMessage(client.player, true, packet);
		QuiltChatEvents.AFTER_PROCESS.invoke(message);
	}

	@ModifyVariable(
			method = "onSystemMessage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
					shift = At.Shift.AFTER
			),
			argsOnly = true
	)
	public SystemMessageS2CPacket quilt$modifyInboundSystemMessage(SystemMessageS2CPacket packet) {
		var message = new SystemS2CMessage(client.player, true, packet);
		return (SystemMessageS2CPacket) QuiltChatEvents.MODIFY.invokeOrElse(message, message).serialized();
	}

	@Inject(
			method = "onSystemMessage",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;client:Lnet/minecraft/client/MinecraftClient;",
					opcode = Opcodes.GETFIELD,
					ordinal = 1,
					shift = At.Shift.BEFORE
			),
			cancellable = true
	)
	public void quilt$cancelAndBeforeInboundSystemMessage(SystemMessageS2CPacket packet, CallbackInfo ci) {
		var message = new SystemS2CMessage(client.player, true, packet);
		if (QuiltChatEvents.CANCEL.invoke(message) == Boolean.TRUE) {
			QuiltChatEvents.CANCELLED.invoke(message);
			ci.cancel();
			return;
		}

		QuiltChatEvents.BEFORE_PROCESS.invoke(message);
	}

	@Inject(
			method = "onSystemMessage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/network/ChatListener;handleSystemMessage(Lnet/minecraft/text/Text;Z)V",
					shift = At.Shift.AFTER
			)
	)
	public void quilt$afterInboundSystemMessage(SystemMessageS2CPacket packet, CallbackInfo ci) {
		var message = new SystemS2CMessage(client.player, true, packet);
		QuiltChatEvents.AFTER_PROCESS.invoke(message);
	}

	@ModifyVariable(
			method = "onProfileIndependentMessage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
					shift = At.Shift.AFTER
			),
			argsOnly = true
	)
	public ProfileIndependentMessageS2CPacket quilt$modifyInboundProfileIndependentMessage(ProfileIndependentMessageS2CPacket packet) {
		var message = new ProfileIndependentS2CMessage(client.player, true, packet);
		return (ProfileIndependentMessageS2CPacket) QuiltChatEvents.MODIFY.invokeOrElse(message, message).serialized();
	}

	@Inject(
			method = "onProfileIndependentMessage",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;client:Lnet/minecraft/client/MinecraftClient;",
					shift = At.Shift.BEFORE,
					ordinal = 1
			),
			cancellable = true
	)
	public void quilt$cancelInboundProfileIndependentMessage(ProfileIndependentMessageS2CPacket packet, CallbackInfo ci) {
		var message = new ProfileIndependentS2CMessage(client.player, true, packet);
		if (QuiltChatEvents.CANCEL.invoke(message) == Boolean.TRUE) {
			QuiltChatEvents.CANCELLED.invoke(message);
			ci.cancel();
		}
	}

	@Inject(
			method = "onProfileIndependentMessage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/MinecraftClient;getChatListener()Lnet/minecraft/client/network/ChatListener;"
			)
	)
	public void quilt$beforeInboundProfileIndependentMessage(ProfileIndependentMessageS2CPacket packet, CallbackInfo ci) {
		var message = new ProfileIndependentS2CMessage(client.player, true, packet);
		QuiltChatEvents.BEFORE_PROCESS.invoke(message);
	}

	@Inject(
			method = "onProfileIndependentMessage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/network/ChatListener;handleMessage(Lnet/minecraft/text/Text;Lnet/minecraft/network/message/MessageType$Parameters;)V",
					shift = At.Shift.AFTER
			)
	)
	public void quilt$afterInboundProfileIndependentMessage(ProfileIndependentMessageS2CPacket packet, CallbackInfo ci) {
		var message = new ProfileIndependentS2CMessage(client.player, true, packet);
		QuiltChatEvents.AFTER_PROCESS.invoke(message);
	}

	@ModifyVariable(method = "sendChatMessage", at = @At("HEAD"), argsOnly = true)
	public String quilt$modifyOutboundRawChatMessage(String string) {
		// Not sure *why* this would be null but IDEA is complaining, so, safety first?
		if (client.player == null) {
			return string;
		}

		var message = new RawChatC2SMessage(client.player, true, string);
		return ((RawChatC2SMessage) QuiltChatEvents.MODIFY.invokeOrElse(message, message)).serialized();
	}

	@Inject(
		method = "sendChatMessage",
		at = @At(value = "INVOKE", target = "Ljava/time/Instant;now()Ljava/time/Instant;"),
		cancellable = true
	)
	public void quilt$cancelAndBeforeOutboundRawChatMessage(String string, CallbackInfo ci) {
		if (client.player == null) return;

		var message = new RawChatC2SMessage(client.player, true, string);
		if (QuiltChatEvents.CANCEL.invoke(message) == Boolean.TRUE) {
			QuiltChatEvents.CANCELLED.invoke(message);
			ci.cancel();
			return;
		}

		QuiltChatEvents.BEFORE_PROCESS.invoke(message);
	}

	@Inject(method = "sendChatMessage", at = @At(value = "TAIL"))
	public void quilt$afterOutboundRawChatMessage(String string, CallbackInfo ci) {
		if (client.player == null) return;

		var message = new RawChatC2SMessage(client.player, true, string);
		QuiltChatEvents.AFTER_PROCESS.invoke(message);
	}

	@Inject(
		method = "sendChatMessage",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/message/LastSeenMessageTracker;update()Lnet/minecraft/network/message/LastSeenMessageTracker$Update;",
			shift = At.Shift.BEFORE
		)
	)
	public void quilt$saveChatSecurityState(String string, CallbackInfo ci) {
		((ChatSecurityRollbackSupport)lastSeenMessageTracker).saveState();
		var chain = MessageChainReverseLookup.getChainFromPacker(messageChainPacker);
		if (chain != null) {
			((ChatSecurityRollbackSupport) chain).saveState();
		}
	}

	@Redirect(
			method = "sendChatMessage",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"
			)
	)
	public void quilt$modifyAndCancelAndBeforeAndAfterOutboundChatMessage(ClientPlayNetworkHandler instance, Packet<?> packet) {
		if (packet instanceof ChatMessageC2SPacket chatMessageC2SPacket) {
			var message = new ChatC2SMessage(client.player, true, chatMessageC2SPacket);
			message = (ChatC2SMessage) QuiltChatEvents.MODIFY.invokeOrElse(message, message);

			if (QuiltChatEvents.CANCEL.invoke(message) != Boolean.TRUE) {
				((ChatSecurityRollbackSupport)lastSeenMessageTracker).dropSavedState();
				var chain = MessageChainReverseLookup.getChainFromPacker(messageChainPacker);
				if (chain != null) {
					((ChatSecurityRollbackSupport) chain).dropSavedState();
				}
				QuiltChatEvents.BEFORE_PROCESS.invoke(message);
				instance.sendPacket(message.serialized());
				QuiltChatEvents.AFTER_PROCESS.invoke(message);
			} else {
				((ChatSecurityRollbackSupport)lastSeenMessageTracker).rollbackState();
				var chain = MessageChainReverseLookup.getChainFromPacker(messageChainPacker);
				if (chain != null) {
					((ChatSecurityRollbackSupport) chain).rollbackState();
				}
				QuiltChatEvents.CANCELLED.invoke(message);
			}
		} else {
			throw new IllegalArgumentException(
				"Received non-ChatMessageC2SPacket for argument to ClientPlayNetworkHandler.sendPacket"
			);
		}
	}

	@ModifyVariable(
		method = "onMessageRemoval",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/NetworkThreadUtils;forceMainThread(Lnet/minecraft/network/packet/Packet;Lnet/minecraft/network/listener/PacketListener;Lnet/minecraft/util/thread/ThreadExecutor;)V",
			shift = At.Shift.AFTER
		),
		argsOnly = true
	)
	public MessageRemovalS2CPacket quilt$modifyInboundMessageRemoval(MessageRemovalS2CPacket packet) {
		var message = new RemovalS2CMessage(client.player, true, packet);
		return (MessageRemovalS2CPacket) QuiltChatEvents.MODIFY.invokeOrElse(message, message).serialized();
	}

	@Inject(
		method = "onMessageRemoval",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/packet/s2c/play/MessageRemovalS2CPacket;signature()Lnet/minecraft/network/message/MessageSignature$Indexed;",
			shift = At.Shift.BEFORE
		),
		cancellable = true
	)
	public void quilt$cancelAndBeforeInboundMessageRemoval(MessageRemovalS2CPacket packet, CallbackInfo ci) {
		var message = new RemovalS2CMessage(client.player, true, packet);
		if (QuiltChatEvents.CANCEL.invoke(message) == Boolean.TRUE) {
			QuiltChatEvents.CANCELLED.invoke(message);
			ci.cancel();

			Optional<MessageSignature> optional = packet.signature().get(this.messageSignatureStorage);
			if (optional.isPresent()) {
				lastSeenMessageTracker.removePending(optional.get());
			} else {
				this.connection.disconnect(INVALID_PACKET_DISCONNECT);
			}
		} else {
			QuiltChatEvents.BEFORE_PROCESS.invoke(message);
		}
	}

	@Inject(method = "onMessageRemoval", at = @At("TAIL"))
	public void quilt$afterInboundMessageRemoval(MessageRemovalS2CPacket packet, CallbackInfo ci) {
		var message = new RemovalS2CMessage(client.player, true, packet);
		QuiltChatEvents.AFTER_PROCESS.invoke(message);
	}

	@Inject(
		method = "sendChatCommand",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/message/LastSeenMessageTracker;update()Lnet/minecraft/network/message/LastSeenMessageTracker$Update;"
		)
	)
	public void quilt$saveCommandSecurityState(String command, CallbackInfo ci) {
		((ChatSecurityRollbackSupport)lastSeenMessageTracker).saveState();
		var chain = MessageChainReverseLookup.getChainFromPacker(messageChainPacker);
		if (chain != null) {
			((ChatSecurityRollbackSupport) chain).saveState();
		}
	}

	@Inject(
		method = "sendCommand",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/network/message/LastSeenMessageTracker;update()Lnet/minecraft/network/message/LastSeenMessageTracker$Update;"
		)
	)
	public void quilt$saveCommandSecurityState(String command, CallbackInfoReturnable<Boolean> cir) {
		((ChatSecurityRollbackSupport)lastSeenMessageTracker).saveState();
		var chain = MessageChainReverseLookup.getChainFromPacker(messageChainPacker);
		if (chain != null) {
			((ChatSecurityRollbackSupport) chain).saveState();
		}
	}

	@Redirect(
		method = {"sendCommand", "sendChatCommand"},
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/network/ClientPlayNetworkHandler;sendPacket(Lnet/minecraft/network/packet/Packet;)V"
		)
	)
	private void quilt$modifyAndCancelAndBeforeAndAfterOutboundCommand(ClientPlayNetworkHandler instance, Packet<?> packet) {
		if (packet instanceof ChatCommandC2SPacket chatCommandC2SPacket) {
			var message = new CommandC2SMessage(client.player, true, chatCommandC2SPacket);
			message = (CommandC2SMessage) QuiltChatEvents.MODIFY.invokeOrElse(message, message);

			if (QuiltChatEvents.CANCEL.invoke(message) != Boolean.TRUE) {
				((ChatSecurityRollbackSupport)lastSeenMessageTracker).dropSavedState();
				var chain = MessageChainReverseLookup.getChainFromPacker(messageChainPacker);
				if (chain != null) {
					((ChatSecurityRollbackSupport) chain).dropSavedState();
				}
				QuiltChatEvents.BEFORE_PROCESS.invoke(message);
				instance.sendPacket(message.serialized());
				QuiltChatEvents.AFTER_PROCESS.invoke(message);
			} else {
				((ChatSecurityRollbackSupport)lastSeenMessageTracker).rollbackState();
				var chain = MessageChainReverseLookup.getChainFromPacker(messageChainPacker);
				if (chain != null) {
					((ChatSecurityRollbackSupport) chain).rollbackState();
				}
				QuiltChatEvents.CANCELLED.invoke(message);
			}
		} else {
			throw new IllegalArgumentException(
				"Received non-ChatCommandC2SPacket for argument to ClientPlayNetworkHandler.sendPacket"
			);
		}
	}
}
