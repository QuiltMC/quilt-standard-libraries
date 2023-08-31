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

package org.quiltmc.qsl.entity.multipart.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

import net.minecraft.entity.Entity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;

import org.quiltmc.qsl.entity.multipart.api.EntityPart;
import org.quiltmc.qsl.entity.multipart.impl.EnderDragonMultipartEntity;

@Mixin(EnderDragonEntity.class)
public abstract class EnderDragonEntityMixin implements EnderDragonMultipartEntity {
	@Shadow
	public abstract EnderDragonPart[] getBodyParts();

	@Override
	public EntityPart<?>[] getEntityParts() {
		return this.getBodyParts();
	}

	/**
	 * Fixes <a href="https://bugs.mojang.com/browse/MC-225055">MC-225055</a>
	 * <p>
	 * The vanilla method sets the {@link EnderDragonEntity#head} to
	 * the same {@link Entity#getId() id} as the {@link EnderDragonEntity} herself.
	 * <p>
	 * This causes an id desync between client and server,
	 * and so the server thinks the client is trying to hit the wrong part.
	 */
	@ModifyArg(method = "onSpawnPacket", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/boss/dragon/EnderDragonPart;setId(I)V"))
	private int setProperId(int oldId) {
		return oldId + 1;
	}
}
