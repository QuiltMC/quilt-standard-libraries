package org.quiltmc.qsl.networking.mixin;

import java.util.HashMap;
import java.util.Map;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.impl.payload.PacketByteBufPayload;

@Mixin(CustomPayloadC2SPacket.class)
public class CustomPayloadC2SPacketMixin {
	@Shadow
	@Final
	@Mutable
	private static Map<Identifier, PacketByteBuf.Reader<? extends CustomPayload>> KNOWN_TYPES;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void makeMutable(CallbackInfo ci) {
		KNOWN_TYPES = new HashMap<>(KNOWN_TYPES);
	}

	@Inject(method = "readPayload", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/c2s/common/CustomPayloadC2SPacket;readUnknownPayload(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/network/packet/payload/DiscardedCustomPayload;"), cancellable = true)
	private static void inject(Identifier id, PacketByteBuf buf, CallbackInfoReturnable<PacketByteBufPayload> cir) {
		cir.setReturnValue(new PacketByteBufPayload(id, buf));
	}
}
