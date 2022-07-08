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

package org.quiltmc.qsl.multipart.test.mixin;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.world.World;
import org.quiltmc.qsl.multipart.api.EntityPart;
import org.quiltmc.qsl.multipart.api.MultipartEntity;
import org.quiltmc.qsl.multipart.test.SecretCreeperPart;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(CreeperEntity.class)
public abstract class CreeperEntityMixin extends HostileEntity implements MultipartEntity {
	private final SecretCreeperPart secretHitbox = new SecretCreeperPart((CreeperEntity) (Object) this, 0.65f, 0.65f);

	protected CreeperEntityMixin(EntityType<? extends HostileEntity> entityType, World world) {
		super(entityType, world);
	}

	@Override
	public EntityPart<?>[] getEntityParts() {
		return new EntityPart[] { secretHitbox };
	}

	@Override
	public void tickMovement() {
		super.tickMovement();
		this.secretHitbox.setPosition(this.getX(), this.getY() + 1.1, this.getZ());
		this.secretHitbox.prevX = this.prevX;
		this.secretHitbox.prevY = this.prevY + 1.1;
		this.secretHitbox.prevZ = this.prevZ;
		this.secretHitbox.lastRenderX = this.lastRenderX;
		this.secretHitbox.lastRenderY = this.lastRenderY + 1.1;
		this.secretHitbox.lastRenderZ = this.lastRenderZ;
	}

	@Override
	public void onSpawnPacket(EntitySpawnS2CPacket packet) {
		super.onSpawnPacket(packet);
		EntityPart<?>[] parts = this.getEntityParts();

		for(int i = 0; i < parts.length; ++i) {
			if (parts[i] instanceof Entity entity) {
				entity.setId(i + 1 + packet.getId());
			}
		}
	}
}
