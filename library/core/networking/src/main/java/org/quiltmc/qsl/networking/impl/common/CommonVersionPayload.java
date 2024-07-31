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

package org.quiltmc.qsl.networking.impl.common;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

public record CommonVersionPayload(int[] versions) implements CustomPayload {
	public static final PacketCodec<PacketByteBuf, CommonVersionPayload> CODEC = CustomPayload.create(CommonVersionPayload::write, CommonVersionPayload::new);
	public static final CustomPayload.Id<CommonVersionPayload> PACKET_ID = new Id<>(Identifier.of("c", "version"));

	public CommonVersionPayload(PacketByteBuf buf) {
		this(buf.readIntArray());
	}

	private void write(PacketByteBuf buf) {
		buf.writeIntArray(this.versions);
	}

	@Override
	public CustomPayload.Id<?> getId() {
		return PACKET_ID;
	}
}
