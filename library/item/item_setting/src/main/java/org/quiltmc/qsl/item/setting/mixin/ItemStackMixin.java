/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.item.setting.mixin;

import java.util.function.Consumer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.item.setting.api.CustomDamageHandler;
import org.quiltmc.qsl.item.setting.impl.CustomItemSettingImpl;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {
	@Shadow
	public abstract Item getItem();

	@Unique
	private LivingEntity quilt$damagingEntity;

	@Unique
	private Consumer<LivingEntity> quilt$breakCallback;

	@Inject(method = "damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("HEAD"))
	private void saveDamager(int amount, LivingEntity entity, Consumer<LivingEntity> breakCallback, CallbackInfo ci) {
		this.quilt$damagingEntity = entity;
		this.quilt$breakCallback = breakCallback;
	}

	@ModifyArg(
			method = "damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/util/random/RandomGenerator;Lnet/minecraft/server/network/ServerPlayerEntity;)Z"
			),
			index = 0
	)
	private int hookDamage(int amount) {
		CustomDamageHandler handler = CustomItemSettingImpl.CUSTOM_DAMAGE_HANDLER.get(this.getItem());

		if (handler != null) {
			return handler.damage((ItemStack) (Object) this, amount, this.quilt$damagingEntity, this.quilt$breakCallback);
		}

		return amount;
	}

	@Inject(method = "damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V", at = @At("RETURN"))
	private <T extends LivingEntity> void clearDamager(int amount, T entity, Consumer<T> breakCallback, CallbackInfo ci) {
		this.quilt$damagingEntity = null;
		this.quilt$breakCallback = null;
	}
}
