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

import java.util.List;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.c2s.common.CustomPayloadC2SPacket;
import net.minecraft.network.packet.payload.CustomPayload;

import org.quiltmc.qsl.networking.impl.PayloadTypeRegistryImpl;
import org.quiltmc.qsl.networking.impl.QuiltCustomPayloadPacketCodec;

@Mixin(CustomPayloadC2SPacket.class)
public class CustomPayloadC2SPacketMixin {
	@WrapOperation(
			method = "<clinit>",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/network/packet/payload/CustomPayload;create(Lnet/minecraft/network/packet/payload/CustomPayload$CodecFactory;Ljava/util/List;)Lnet/minecraft/network/codec/PacketCodec;"
			)
	)
	@SuppressWarnings("unchecked")
	private static PacketCodec<PacketByteBuf, CustomPayload> wrapCodec(CustomPayload.CodecFactory<PacketByteBuf> unknownCodecFactory, List<CustomPayload.Type<PacketByteBuf, ?>> types, Operation<PacketCodec<PacketByteBuf, CustomPayload>> original) {
		PacketCodec<PacketByteBuf, CustomPayload> codec = original.call(unknownCodecFactory, types);
		QuiltCustomPayloadPacketCodec<PacketByteBuf> quiltCodec = (QuiltCustomPayloadPacketCodec<PacketByteBuf>) codec;
		quiltCodec.setPacketCodecProvider((packetByteBuf, identifier) -> {
			// CustomPayloadC2SPacket does not have a separate codec for play/configuration. We know if the packetByteBuf is a PacketByteBuf we are in the play phase.
			if (packetByteBuf instanceof RegistryByteBuf) {
				return (CustomPayload.Type<PacketByteBuf, ? extends CustomPayload>) (Object) PayloadTypeRegistryImpl.PLAY_C2S.get(identifier);
			}

			return PayloadTypeRegistryImpl.CONFIGURATION_C2S.get(identifier);
		});
		return codec;
	}
}
