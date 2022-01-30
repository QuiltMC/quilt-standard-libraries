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

package org.quiltmc.qsl.bows.api;

import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

/**
 * An interface to implement for all custom crossbows in Quilt. <br>
 * This is meant to be used on a {@link net.minecraft.item.CrossbowItem} class. Unless similar functionality is implemented on your custom item, most functionality will not work. <br>
 * In order to modify the projectile shot from your crossbow, implementing {@link ShotProjectileEvents.ModifyProjectileFromCrossbow} and registering it is recommended.
 *
 * @see ExtendedCrossbowItem
 */
public interface CrossbowExtensions extends ShotProjectileEvents.ModifyProjectileFromCrossbow {
	/**
	 * Allows editing of the projectile entity shot from the crossbow. Applies all crossbow
	 * projectile properties first.
	 *
	 * @param crossbowStack              the ItemStack for the crossbow
	 * @param projectileStack            the stack for the projectile
	 * @param entity                     the entity shooting the crossbow
	 * @param persistentProjectileEntity the projectile entity to be shot
	 */
	void modifyProjectileShot(ItemStack crossbowStack, ItemStack projectileStack, LivingEntity entity, @NotNull PersistentProjectileEntity persistentProjectileEntity);

	/**
	 * Allows modifying the speed of the crossbow projectile. <br>
	 * To get the projectile from the crossbow, call {@link CrossbowItem#hasProjectile(ItemStack, Item)} passing in {@code stack} and the {@link Item} for the projectile. <br>
	 * The default implementation follows the vanilla values for the projectiles
	 *
	 * @param stack  the ItemStack for the crossbow
	 * @param entity the Entity shooting the crossbow
	 * @return the speed of the projectile
	 */
	default float getProjectileSpeed(ItemStack stack, LivingEntity entity) {
		return stack.getItem() == Items.CROSSBOW && CrossbowItem.hasProjectile(stack, Items.FIREWORK_ROCKET) ? 1.6F : 3.15F;
	}
}
