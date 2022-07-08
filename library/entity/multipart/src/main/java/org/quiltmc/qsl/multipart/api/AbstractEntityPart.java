package org.quiltmc.qsl.multipart.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;

/**
 * A partial implementation of an {@link EntityPart} with the most common methods implemented.
 * @param <E> The {@link Entity} that owns this {@link EntityPart}
 * @see EnderDragonPart
 */
public abstract class AbstractEntityPart<E extends Entity> extends Entity implements EntityPart<E> {
	private final E owner;
	private final EntityDimensions partDimensions;

	public AbstractEntityPart(E owner, float width, float height) {
		super(owner.getType(), owner.world);
		this.partDimensions = EntityDimensions.changing(width, height);
		this.calculateDimensions();
		this.owner = owner;
	}

	@Override
	public E getOwner() {
		return this.owner;
	}

	@Override
	protected void initDataTracker() {

	}

	@Override
	protected void readCustomDataFromNbt(NbtCompound nbt) {

	}

	@Override
	protected void writeCustomDataToNbt(NbtCompound nbt) {

	}

	@Override
	public boolean collides() {
		return true;
	}

	@Override
	public boolean damage(DamageSource source, float amount) {
		return !this.isInvulnerableTo(source) && this.owner.damage(source, amount);
	}

	@Override
	public boolean isPartOf(Entity entity) {
		return this == entity || this.owner == entity;
	}

	@Override
	public Packet<?> createSpawnPacket() {
		throw new UnsupportedOperationException();
	}

	@Override
	public EntityDimensions getDimensions(EntityPose pose) {
		return this.partDimensions;
	}

	@Override
	public boolean shouldSave() {
		return false;
	}
}
