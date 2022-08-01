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

import org.quiltmc.qsl.component.impl.ComponentsImpl;
import org.quiltmc.qsl.component.impl.container.LazyComponentContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.world.World;

import org.quiltmc.qsl.component.api.container.ComponentContainer;
import org.quiltmc.qsl.component.api.provider.ComponentProvider;
import org.quiltmc.qsl.component.api.sync.SyncChannel;

@Mixin(Entity.class)
public abstract class EntityMixin implements ComponentProvider {
	@Shadow
	public World world;
	private ComponentContainer qsl$container;

	@Override
	public ComponentContainer getComponentContainer() {
		return this.qsl$container;
	}

	@Inject(method = "<init>", at = @At("RETURN"))
	private void onEntityInit(EntityType<?> entityType, World world, CallbackInfo ci) {
		var builder = ComponentContainer.builder(this)
				.acceptsInjections();

		if (!world.isClient) {
			builder.syncing(SyncChannel.ENTITY).ticking();
		}

		this.qsl$container = builder.build(ComponentsImpl.DEFAULT_FACTORY);
	}

	@Inject(method = "writeNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;writeCustomDataToNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
	private void onSerialize(NbtCompound nbt, CallbackInfoReturnable<NbtCompound> cir) {
		this.getComponentContainer().writeNbt(nbt);
	}

	@Inject(method = "readNbt", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;readCustomDataFromNbt(Lnet/minecraft/nbt/NbtCompound;)V"))
	private void onDeserialize(NbtCompound nbt, CallbackInfo ci) {
		this.getComponentContainer().readNbt(nbt);
	}

	@Inject(method = "tick", at = @At("TAIL"))
	private void tickContainer(CallbackInfo ci) {
		if (!this.world.isClient) {
			this.getComponentContainer().tick(this);
		}
	}
}
