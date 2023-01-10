package org.quiltmc.qsl.chat.mixin;

import net.minecraft.network.Packet;
import net.minecraft.network.PacketSendListener;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.quiltmc.qsl.chat.api.ChatEvents;
import org.quiltmc.qsl.chat.api.types.s2c.ImmutableSystemMessage;
import org.quiltmc.qsl.chat.api.types.s2c.MutableSystemMessage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// TODO: Hook outbound system message
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
	@Shadow
	public ServerPlayNetworkHandler networkHandler;

	private ImmutableSystemMessage quilt$sendSystemMessage$storedSystemMessage;

	@Inject(method = "sendSystemMessage(Lnet/minecraft/text/Text;Z)V", at = @At("HEAD"))
	public void quilt$captureAndModifyOutboundSystemMessage(Text originalMessage, boolean overlay, CallbackInfo ci) {
		MutableSystemMessage message = new MutableSystemMessage(networkHandler, originalMessage, overlay);

		ChatEvents.MODIFY.invoke(message);

		quilt$sendSystemMessage$storedSystemMessage = message.immutableCopy();
	}

	@Redirect(method = "sendSystemMessage(Lnet/minecraft/text/Text;Z)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/network/ServerPlayNetworkHandler;sendPacket(Lnet/minecraft/network/Packet;Lnet/minecraft/network/PacketSendListener;)V"))
	public void quilt$cancelOutboundSystemMessage(ServerPlayNetworkHandler instance, Packet<?> packet, PacketSendListener listener) {
		if (!ChatEvents.CANCEL.invoke(quilt$sendSystemMessage$storedSystemMessage)) {
			instance.sendPacket(quilt$sendSystemMessage$storedSystemMessage.packet(), listener);
		}
	}
}
