package org.quiltmc.qsl.entity.custom_spawn_data.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.network.Packet;
import net.minecraft.world.World;

/**
 * A small helper class to keep your implementation clean. Handles spawn packet creation for you.
 */
public abstract class AbstractCustomSpawnDataEntity extends Entity implements QuiltCustomSpawnDataEntity {
	public AbstractCustomSpawnDataEntity(EntityType<?> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public Packet<?> createSpawnPacket() {
		return makeCustomSpawnPacket();
	}
}
