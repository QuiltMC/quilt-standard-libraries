package org.quiltmc.qsl.entity.networking.api.custom_spawn_data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.world.World;

/**
 * A small helper class to keep your implementation clean. Handles spawn packet creation for you.
 */
public abstract class BaseCustomSpawnDataEntity extends Entity implements QuiltCustomSpawnDataEntity {
	public BaseCustomSpawnDataEntity(EntityType<?> variant, World world) {
		super(variant, world);
	}

	@Override
	public Packet<ClientPlayPacketListener> createSpawnPacket() {
		return makeCustomSpawnPacket();
	}
}
