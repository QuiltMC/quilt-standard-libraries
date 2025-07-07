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

package org.quiltmc.qsl.entity.test.mixin.networking;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.data.DataTracker;
import net.minecraft.entity.data.TrackedData;
import net.minecraft.entity.data.TrackedDataHandlerRegistry;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.particle.ParticleEffect;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;

import org.quiltmc.qsl.entity.extensions.api.networking.QuiltExtendedSpawnDataEntity;
import org.quiltmc.qsl.entity.test.networking.CreeperWithItem;

/**
 * In actual mods, do not add tracked data to existing entities, and do not replace spawn packets.
 * This is purely for testing QSL easily.
 */
@Mixin(CreeperEntity.class)
abstract class CreeperEntityMixin extends HostileEntity implements QuiltExtendedSpawnDataEntity, CreeperWithItem {
	// Make creepers store a particle effect to test a custom tracked data handler

	@SuppressWarnings("WrongEntityDataParameterClass")
	@Unique
	private static final TrackedData<ParticleEffect> PARTICLE = DataTracker
			.registerData(CreeperEntity.class, TrackedDataHandlerRegistry.PARTICLE);

	@SuppressWarnings("DataFlowIssue")
	private CreeperEntityMixin() {
		super(null, null);
		throw new AssertionError("dummy constructor called");
	}

	@Inject(method = "initDataTracker", at = @At("TAIL"))
	private void quiltTestMod$addCustomTracker(DataTracker.Builder builder, CallbackInfo ci) {
		builder.add(PARTICLE, ParticleTypes.ANGRY_VILLAGER);
	}

	@Inject(method = "interactMob", at = @At("HEAD"))
	private void quiltTestMod$addCustomTracker(PlayerEntity player, Hand hand, CallbackInfoReturnable<ActionResult> cir) {
		if (this.getWorld().isClient) {
			this.getWorld().addParticle(this.dataTracker.get(PARTICLE), this.getX(), this.getY() + 2, this.getZ(), 0, 0, 0);
		} else {
			if (player.getStackInHand(hand).getItem() == Items.STICK) {
				this.dataTracker.set(PARTICLE, ParticleTypes.CRIT);
			} else if (player.getStackInHand(hand).getItem() == Items.TNT) {
				this.dataTracker.set(PARTICLE, ParticleTypes.SMOKE);
			}
		}
	}

	// Make creepers drop a random item on explosion and render it over their head to test extended spawn data

	@Unique
	private ItemStack stackToDrop;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void storeRandomItem(CallbackInfo ci) {
		final Item random = Registries.ITEM.getRandom(this.random).orElseThrow().getValue();
		this.stackToDrop = new ItemStack(random);
	}

	@Inject(method = "explode", at = @At("TAIL"))
	private void dropItemOnExplosion(CallbackInfo ci) {
		if (this.getWorld() instanceof ServerWorld world) {
			this.dropStack(world, this.stackToDrop);
		}
	}

	// overrides api method
	@SuppressWarnings("AddedMixinMembersNamePattern")
	@Override
	public void writeAdditionalSpawnData(RegistryByteBuf buffer) {
		ItemStack.OPTIONAL_PACKET_CODEC.encode(buffer, this.stackToDrop);
	}

	// overrides api method
	@SuppressWarnings("AddedMixinMembersNamePattern")
	@Override
	public void readAdditionalSpawnData(RegistryByteBuf buffer) {
		this.stackToDrop = ItemStack.PACKET_CODEC.decode(buffer);
	}

	@Override
	public ItemStack quilt$getStack() {
		return this.stackToDrop;
	}
}
