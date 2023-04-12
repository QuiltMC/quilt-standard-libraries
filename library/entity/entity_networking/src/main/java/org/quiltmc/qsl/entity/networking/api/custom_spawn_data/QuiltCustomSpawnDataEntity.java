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

package org.quiltmc.qsl.entity.networking.api.custom_spawn_data;

import net.minecraft.entity.Entity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

/**
 * An entity with additional data sent in its spawn packet. To use, simply implement this interface on your entity.
 * If not overridden, {@link Entity#createSpawnPacket()} will return a packet containing the written data.
 */
public interface QuiltCustomSpawnDataEntity {
	/**
	 * The ID of the packet used to spawn entities with custom spawn data on the client.
	 */
	Identifier EXTENDED_SPAWN_PACKET = new Identifier("quilt", "extended_entity_spawn_packet");

	/**
	 * Write additional data to be sent when this entity spawns. Will be deserialized on the client by
	 * {@link QuiltCustomSpawnDataEntity#readCustomSpawnData(PacketByteBuf)}
	 */
	void writeCustomSpawnData(PacketByteBuf buffer);

	/**
	 * Read additional data written on the server by {@link QuiltCustomSpawnDataEntity#writeCustomSpawnData},
	 * and deserialize it on the client after the entity is spawned.
	 */
	void readCustomSpawnData(PacketByteBuf buffer);
}
