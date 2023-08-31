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

package org.quiltmc.qsl.entity.networking.api.extended_spawn_data;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.PacketBundleS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;

import org.quiltmc.qsl.entity.networking.impl.QuiltEntityNetworkingInitializer;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

/**
 * An entity with additional data sent in its spawn packet. To use, simply implement this interface on your entity.
 * If not overridden, {@link Entity#createSpawnPacket()} will return a packet containing the written data.
 */
public interface QuiltExtendedSpawnDataEntity {
	/**
	 * Write additional data to be sent when this entity spawns. Will be deserialized on the client by
	 * {@link #readAdditionalSpawnData(PacketByteBuf)}
	 */
	void writeAdditionalSpawnData(PacketByteBuf buffer);

	/**
	 * Read additional data written on the server by {@link #writeAdditionalSpawnData(PacketByteBuf)},
	 * and deserialize it on the client after the entity is spawned.
	 */
	void readAdditionalSpawnData(PacketByteBuf buffer);

	/**
	 * Given an entity with extra spawn data and a base spawn packet, create an extended spawn packet.
	 * By default, a mod does not need to touch this, as packet creation is handled by QSL.
	 * However, a mod may want to use a different base packet, such as a different constructor of {@link EntitySpawnS2CPacket}.
	 * In that case, override {@link Entity#createSpawnPacket()} and call this method.
	 */
	static Packet<ClientPlayPacketListener> createExtendedPacket(QuiltExtendedSpawnDataEntity extended,
																 Packet<ClientPlayPacketListener> basePacket) {
		if (!(extended instanceof Entity entity)) {
			throw new IllegalArgumentException(extended.getClass() + " does not extend Entity!");
		}

		var buf = PacketByteBufs.create();
		buf.writeVarInt(entity.getId());
		extended.writeAdditionalSpawnData(buf);
		var additionalPacket = ServerPlayNetworking.createS2CPacket(
				QuiltEntityNetworkingInitializer.EXTENDED_SPAWN_PACKET_ID, buf
		);

		return new PacketBundleS2CPacket(List.of(basePacket, additionalPacket));
	}
}
