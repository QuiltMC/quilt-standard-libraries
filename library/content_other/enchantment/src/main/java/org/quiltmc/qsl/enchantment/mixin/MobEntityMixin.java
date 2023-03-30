/*
 * Copyright 2023 QuiltMC
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

package org.quiltmc.qsl.enchantment.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;

import org.quiltmc.qsl.enchantment.api.EntityEnchantingContext;
import org.quiltmc.qsl.enchantment.impl.EnchantmentGodClass;

@Mixin(MobEntity.class)
public abstract class MobEntityMixin extends LivingEntity {
	@Shadow
	public abstract ItemStack getEquippedStack(EquipmentSlot slot);

	protected MobEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "enchantMainHandItem", at = @At("HEAD"))
	private void setEnchantingContextForMainHand(RandomGenerator random, float power, CallbackInfo ci) {
		EnchantmentGodClass.context.set(new EntityEnchantingContext<>(0, 0, this.getMainHandStack(), this.world, this.getRandom(), false, this));
	}

	@Inject(method = "enchantMainHandItem", at = @At("RETURN"))
	private void removeEnchantingContextForMainHand(RandomGenerator random, float power, CallbackInfo ci) {
		EnchantmentGodClass.context.remove();
	}

	@Inject(method = "enchantEquipment", at = @At("HEAD"))
	private void setEnchantingContextForEquipment(RandomGenerator random, float power, EquipmentSlot slot, CallbackInfo ci) {
		EnchantmentGodClass.context.set(new EntityEnchantingContext<>(0, 0, this.getEquippedStack(slot), this.world, this.getRandom(), false, this));
	}

	@Inject(method = "enchantEquipment", at = @At("RETURN"))
	private void removeEnchantingContextForEquipment(RandomGenerator random, float power, EquipmentSlot slot, CallbackInfo ci) {
		EnchantmentGodClass.context.remove();
	}
}
