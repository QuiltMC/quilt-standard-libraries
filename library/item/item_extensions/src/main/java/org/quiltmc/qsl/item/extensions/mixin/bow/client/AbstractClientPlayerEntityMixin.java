/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.item.extensions.mixin.bow.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Constant;
import org.spongepowered.asm.mixin.injection.ModifyConstant;
import org.spongepowered.asm.mixin.injection.Redirect;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import org.quiltmc.qsl.item.extensions.api.bow.BowExtensions;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin extends PlayerEntity {
	public AbstractClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
		super(world, pos, yaw, gameProfile);
	}
	
	// Make sure that the fov is changed for custom items
	@Redirect(
		  method = "getSpeed",
		  at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;isOf(Lnet/minecraft/item/Item;)Z")
	)
	private boolean renderFov(ItemStack instance, Item item) {
		if (item == Items.BOW) {
			return instance.getItem() instanceof BowExtensions; // Return bow for fov
		}
		return instance.isOf(item); // Default behavior
	}

	// Modify the draw duration value
	@ModifyConstant(method = "getSpeed", constant = @Constant(floatValue = 20.0F))
	private float modifyDrawDuration(float constant) {
		if ((float) ((BowExtensions) this.getActiveItem().getItem()).getMaxDrawDuration() != constant) {
			return (float) ((BowExtensions) this.getActiveItem().getItem()).getMaxDrawDuration(); // Return custom bow's max draw duration
		}
		return constant; // Default behavior
	}
}
