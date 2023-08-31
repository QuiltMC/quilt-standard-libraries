/*
 * Copyright 2022 The Quilt Project
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

package org.quiltmc.qsl.entity.multipart.test.mixin;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import org.quiltmc.qsl.entity.multipart.api.EntityPart;
import org.quiltmc.qsl.entity.multipart.api.MultipartEntity;
import org.quiltmc.qsl.entity.multipart.test.SecretCreeperPart;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin extends HostileEntity implements MultipartEntity {
	private final SecretCreeperPart secretHitbox = new SecretCreeperPart((CreeperEntity) (Object) this, 0.65f, 0.65f,
			new Vec3d(0.0d, 1.1d, 0.325d), new Vec3d(0.0d, 1.1d, 0.0d));

	protected CreeperEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public EntityPart<?>[] getEntityParts() {
		return new EntityPart[] {this.secretHitbox};
	}

	@Override
	public void tickMovement() {
		super.tickMovement();
		var cycle = 0.5f + (age % 100) / 100f;
		this.secretHitbox.scale(cycle);
		this.secretHitbox.rotate(this.getPitch(), this.getHeadYaw(), true);
	}

	@Override
	public void onSpawnPacket(EntitySpawnS2CPacket packet) {
		super.onSpawnPacket(packet);
		this.secretHitbox.setId(1 + packet.getId());
	}
}
