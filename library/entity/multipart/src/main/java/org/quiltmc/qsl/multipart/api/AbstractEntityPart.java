/*
 * Copyright 2022 QuiltMC
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

package org.quiltmc.qsl.multipart.api;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import net.minecraft.util.math.Quaternion;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3f;

/**
 * A partial implementation of an {@link EntityPart} with the most common methods implemented.
 *
 * @param <E> the {@link Entity} that owns this {@link EntityPart}
 * @see EnderDragonPart
 */
public abstract class AbstractEntityPart<E extends Entity> extends Entity implements EntityPart<E> {
	private final E owner;
	private EntityDimensions partDimensions;
	private float widthRatio = 1.0f;
	private float heightRatio = 1.0f;
	private Vec3d relativePosition = Vec3d.ZERO;
	private Vec3d pivot = Vec3d.ZERO;

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

	/**
	 * Scales the {@link EntityPart}'s {@link EntityDimensions dimensions}.
	 *
	 * @param width  the ratio to scale the bounding width by
	 * @param height the ratio to scale the bounding height by
	 */
	public void scale(float width, float height) {
		this.widthRatio = width;
		this.heightRatio = height;
		this.calculateDimensions();
	}

	/**
	 * Scales the {@link EntityPart}'s {@link EntityDimensions dimensions}.
	 *
	 * @param ratio the ratio to scale the bounding box by
	 */
	public void scale(float ratio) {
		this.scale(ratio, ratio);
	}

	/**
	 * Sets the {@link EntityPart}'s {@link EntityDimensions dimensions}.
	 *
	 * @param width  the bounding width
	 * @param height the bounding height
	 */
	public void setDimensions(float width, float height) {
		this.partDimensions = EntityDimensions.changing(width, height);
		this.calculateDimensions();
	}

	/**
	 * Set the position relative to the {@link AbstractEntityPart#owner owner}.
	 *
	 * @param position the relative position
	 */
	public void setRelativePosition(Vec3d position) {
		this.relativePosition = position;
	}

	/**
	 * Gets the absolute default position for this {@link AbstractEntityPart}.
	 *
	 * @return the absolute position
	 */
	public Vec3d getAbsolutePosition() {
		return this.owner.getPos().add(relativePosition);
	}

	/**
	 * Gets the position relative to the {@link AbstractEntityPart#owner owner}.
	 *
	 * @return the relative position
	 */
	public Vec3d getRelativePosition() {
		return this.relativePosition;
	}

	/**
	 * Gets the relative x position in regard to the {@link AbstractEntityPart#owner owner}.
	 *
	 * @return the relative x position
	 */
	public double getRelativeX() {
		return this.relativePosition.x;
	}

	/**
	 * Gets the relative y position in regard to the {@link AbstractEntityPart#owner owner}.
	 *
	 * @return the relative y position
	 */
	public double getRelativeY() {
		return this.relativePosition.y;
	}

	/**
	 * Gets the relative z position in regard to the {@link AbstractEntityPart#owner owner}.
	 *
	 * @return the relative z position
	 */
	public double getRelativeZ() {
		return this.relativePosition.z;
	}

	/**
	 * A much more computationally simple movement method for entity parts.
	 *
	 * @param distance the distance to move with respect to the {@link AbstractEntityPart#owner owner}'s position
	 */
	public void move(Vec3d distance) {
		this.move(distance.x, distance.y, distance.z);
	}

	/**
	 * A much more computationally simple movement method for entity parts.
	 *
	 * @param dx the distance to move with respect to the {@link AbstractEntityPart#owner owner}'s position along the world x-axis
	 * @param dy the distance to move with respect to the {@link AbstractEntityPart#owner owner}'s position along the world y-axis
	 * @param dz the distance to move with respect to the {@link AbstractEntityPart#owner owner}'s position along the world z-axis
	 */
	public void move(double dx, double dy, double dz) {
		this.prevX = this.lastRenderX = this.getX();
		this.prevY = this.lastRenderY = this.getY();
		this.prevZ = this.lastRenderZ = this.getZ();
		var newPos = this.getAbsolutePosition().add(dx, dy, dz);
		this.setPosition(newPos);
	}

	/**
	 * Rotates this {@link AbstractEntityPart} about the pivot point with the given rotation.
	 *
	 * @param pivot the pivot point to rotate about in relative coordinates
	 * @param pitch the rotation about z-axis in degrees
	 * @param yaw   the rotation about y-axis in degrees
	 * @param roll  the rotation about x-axis in degrees
	 */
	public void rotate(Vec3d pivot, float pitch, float yaw, float roll) {
		this.setPivot(pivot);
		this.rotate(pitch, yaw, roll);
	}

	/**
	 * Rotates this {@link AbstractEntityPart} about its {@link AbstractEntityPart#pivot pivot point} with the given rotation.
	 *
	 * @param pitch the rotation about z-axis in degrees
	 * @param yaw   the rotation about y-axis in degrees
	 * @param roll  the rotation about x-axis in degrees
	 */
	public void rotate(float pitch, float yaw, float roll) {
		Vec3f relativePos = new Vec3f(this.getAbsolutePosition().subtract(this.getAbsolutePivot()));
		relativePos.rotate(new Quaternion(-roll, -yaw, -pitch, true));
		var transformedPos = this.getAbsolutePivot().subtract(this.getAbsolutePosition()).add(relativePos.getX(), relativePos.getY(), relativePos.getZ());

		this.move(transformedPos);
	}

	/**
	 * Gets the pivot point relative to the {@link AbstractEntityPart#owner owner}.
	 *
	 * @return the pivot point
	 */
	public Vec3d getPivot() {
		return pivot;
	}

	/**
	 * Gets the pivot point in absolute coordinates.
	 *
	 * @return the pivot point
	 */
	public Vec3d getAbsolutePivot() {
		return this.owner.getPos().add(pivot);
	}

	/**
	 * Sets the point to {@link AbstractEntityPart#rotate(float, float, float) rotate} about.
	 *
	 * @param pivot the pivot point
	 */
	public void setPivot(Vec3d pivot) {
		this.pivot = pivot;
	}

	/**
	 * Sets the point to {@link AbstractEntityPart#rotate(float, float, float) rotate} about.
	 *
	 * @param x the x coordinate of the pivot point
	 * @param y the y coordinate of the pivot point
	 * @param z the z coordinate of the pivot point
	 */
	public void setPivot(double x, double y, double z) {
		this.pivot = new Vec3d(x, y, z);
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
		if (widthRatio != 1.0f || heightRatio != 1.0f) {
			return this.partDimensions.scaled(widthRatio, heightRatio);
		}
		return this.partDimensions;
	}

	@Override
	public boolean shouldSave() {
		return false;
	}
}
