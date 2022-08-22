package org.quiltmc.qsl.entity.custom_spawn_data.api;

import net.minecraft.entity.Entity;
import net.minecraft.network.Packet;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.Identifier;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;

/**
 * An entity with additional data sent in its spawn packet.
 */
public interface QuiltCustomSpawnDataEntity {
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

	/**
	 * Create an extended spawn packet to be sent to clients.
	 */
	default Packet<?> makeCustomSpawnPacket() {
		if (!(this instanceof Entity self)) {
			throw new IllegalStateException("QuiltCustomSpawnDataEntity implemented on something that isn't an entity");
		}
		PacketByteBuf buf = PacketByteBufs.create();
		new EntitySpawnS2CPacket(self).write(buf);
		writeCustomSpawnData(buf);
		return ServerPlayNetworking.createS2CPacket(EXTENDED_SPAWN_PACKET, buf);
	}
}
