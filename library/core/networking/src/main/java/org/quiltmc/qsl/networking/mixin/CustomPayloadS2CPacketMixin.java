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
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.network.packet.s2c.common.CustomPayloadS2CPacket;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.impl.payload.PacketByteBufPayload;

@Mixin(CustomPayloadS2CPacket.class)
public class CustomPayloadS2CPacketMixin {
	@Shadow
	@Final
	@Mutable
	private static Map<Identifier, PacketByteBuf.Reader<? extends CustomPayload>> KNOWN_TYPES;

	@Inject(method = "<clinit>", at = @At("TAIL"))
	private static void makeMutable(CallbackInfo ci) {
		KNOWN_TYPES = new HashMap<>(KNOWN_TYPES);
	}

	@Inject(method = "readPayload", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/common/CustomPayloadS2CPacket;readUnknownPayload(Lnet/minecraft/util/Identifier;Lnet/minecraft/network/PacketByteBuf;)Lnet/minecraft/network/packet/payload/DiscardedCustomPayload;"), cancellable = true)
	private static void inject(Identifier id, PacketByteBuf buf, CallbackInfoReturnable<PacketByteBufPayload> cir) {
		PacketByteBuf copied = PacketByteBufs.copy(buf);
		cir.setReturnValue(new PacketByteBufPayload(id, copied));
		buf.skipBytes(buf.readableBytes());
	}
}
