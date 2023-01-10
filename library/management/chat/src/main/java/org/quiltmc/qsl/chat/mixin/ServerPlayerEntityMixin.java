package org.quiltmc.qsl.chat.mixin;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketSendListener;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.quiltmc.qsl.chat.api.QuiltChatEvents;
import org.quiltmc.qsl.chat.api.types.ImmutableS2CSystemMessage;
import org.quiltmc.qsl.chat.api.types.MutableS2CSystemMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
	private ImmutableS2CSystemMessage quilt$sendSystemMessage$storedSystemMessage;

	@Inject(method = "sendSystemMessage(Lnet/minecraft/text/Text;Z)V", at = @At("HEAD"))
	public void quilt$captureAndModifyOutboundSystemMessage(Text originalMessage, boolean overlay, CallbackInfo ci) {
		MutableS2CSystemMessage message = new MutableS2CSystemMessage((ServerPlayerEntity)(Object)this, originalMessage, overlay);

		QuiltChatEvents.MODIFY.invoke(message);

		quilt$sendSystemMessage$storedSystemMessage = message.immutableCopy();
	}

	@Redirect(method = "sendSystemMessage(Lnet/minecraft/text/Text;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketSendListener;)V"))
	public void quilt$cancelOutboundSystemMessage(ServerPlayNetworkHandler instance, Packet<?> packet, PacketSendListener listener) {
		if (QuiltChatEvents.CANCEL.invoke(quilt$sendSystemMessage$storedSystemMessage) == Boolean.FALSE) {
			QuiltChatEvents.BEFORE_IO.invoke(quilt$sendSystemMessage$storedSystemMessage);
			instance.sendPacket(quilt$sendSystemMessage$storedSystemMessage.asPacket(), listener);
			QuiltChatEvents.AFTER_IO.invoke(quilt$sendSystemMessage$storedSystemMessage);
		}
	}
}
