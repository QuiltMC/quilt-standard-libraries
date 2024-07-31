/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2024 The Quilt Project
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

package org.quiltmc.qsl.networking.impl;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

public interface QuiltCustomPayloadPacketCodec<B extends PacketByteBuf> {
	void setPacketCodecProvider(CustomPayloadTypeProvider<B> customPayloadTypeProvider);

	interface CustomPayloadTypeProvider<B extends PacketByteBuf> {
		CustomPayload.Type<B, ? extends CustomPayload> get(B packetByteBuf, Identifier identifier);
	}
}
