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

package org.quiltmc.qsl.component.mixin.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;
import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.impl.container.LazyComponentContainer;
import org.quiltmc.qsl.component.impl.sync.SyncPlayerList;
import org.quiltmc.qsl.component.impl.sync.header.SyncPacketHeader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements ComponentProvider {
	private ComponentContainer qsl$container;

	@Override
	public ComponentContainer getComponentContainer() {
		return this.qsl$container;
	}

	@SuppressWarnings("ConstantConditions")
	@Inject(method = "<init>", at = @At("RETURN"))
	private void onEntityInit(EntityType<?> entityType, World world, CallbackInfo ci) {
		this.qsl$container = ComponentContainer.builder(this)
				.unwrap()
				.ticking()
				.syncing(SyncPacketHeader.ENTITY, () -> SyncPlayerList.create((Entity) (Object) this))
				.build(LazyComponentContainer.FACTORY);
	}

	@Inject(method = "writeNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
	private void onSerialize(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
		this.qsl$container.writeNbt(nbt);
	}

	@Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
	private void onDeserialize(NbtCompound nbt, CallbackInfo ci) {
		this.qsl$container.readNbt(nbt);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void tickContainer(CallbackInfo ci) {
		this.qsl$container.tick(this);
	}
}