/*
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

package org.quiltmc.qsl.entity.extensions.impl.networking;

import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.payload.CustomPayload;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.networking.api.PacketByteBufs;

public record ExtendedEntitySpawnPayload(int entityId, RegistryByteBuf data) implements CustomPayload {
	public static final CustomPayload.Id<ExtendedEntitySpawnPayload> ID = new CustomPayload.Id<>(Identifier.of("quilt", "extended_entity_spawn_packet"));
	public static final PacketCodec<RegistryByteBuf, ExtendedEntitySpawnPayload> CODEC = CustomPayload.create(ExtendedEntitySpawnPayload::write, ExtendedEntitySpawnPayload::read);

	public static ExtendedEntitySpawnPayload read(RegistryByteBuf data) {
		return new ExtendedEntitySpawnPayload(data.readVarInt(), new RegistryByteBuf(PacketByteBufs.read(data), data.getRegistryManager()));
	}

	public void write(RegistryByteBuf buf) {
		buf.writeVarInt(this.entityId);
		buf.writeBytes(this.data);
	}

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}
}
