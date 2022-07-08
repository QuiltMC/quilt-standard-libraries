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

package org.quiltmc.qsl.multipart.test;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.Packet;
import org.quiltmc.qsl.multipart.api.EntityPart;

public class SecretCreeperPart extends Entity implements EntityPart<CreeperEntity> {
	private final CreeperEntity owner;
	private final EntityDimensions partDimensions;

	public SecretCreeperPart(CreeperEntity creeper, float f, float g) {
		super(creeper.getType(), creeper.world);
		this.partDimensions = EntityDimensions.changing(f, g);
		this.calculateDimensions();
		this.owner = creeper;
	}

	protected void initDataTracker() {
	}

	protected void readCustomDataFromNbt(NbtCompound nbt) {
	}

	protected void writeCustomDataToNbt(NbtCompound nbt) {
	}

	public boolean collides() {
		return true;
	}

	public boolean damage(DamageSource source, float amount) {
		return !this.isInvulnerableTo(source) && this.owner.damage(source, amount * 10);
	}

	public boolean isPartOf(Entity entity) {
		return this == entity || this.owner == entity;
	}

	public Packet<?> createSpawnPacket() {
		throw new UnsupportedOperationException();
	}

	public EntityDimensions getDimensions(EntityPose pose) {
		return this.partDimensions;
	}

	public boolean shouldSave() {
		return false;
	}

	@Override
	public CreeperEntity getOwner() {
		return owner;
	}
}
