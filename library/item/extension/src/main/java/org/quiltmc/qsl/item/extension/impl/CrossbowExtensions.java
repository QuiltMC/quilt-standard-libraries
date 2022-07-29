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

package org.quiltmc.qsl.item.extension.impl;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.base.api.util.InjectedInterface;
import org.quiltmc.qsl.item.extension.api.ProjectileModifyingCrossbowItem;
import org.quiltmc.qsl.item.extension.api.ShotProjectileEvents;
import org.quiltmc.qsl.item.extension.mixin.CrossbowItemAccessor;

/**
 * An interface to implement for all custom crossbows in Quilt.
 * <p>
 * This is meant to be used on a {@link net.minecraft.item.CrossbowItem} class.</p>
 * <p>
 * In order to modify the projectile shot from your crossbow, implementing and registering {@link ShotProjectileEvents.ModifyProjectileFromCrossbow} is recommended.</p>
 *
 * @see ProjectileModifyingCrossbowItem
 */
@ApiStatus.Internal
@InjectedInterface(CrossbowItem.class)
public interface CrossbowExtensions {
	/**
	 * Allows modifying the speed of the crossbow projectile. <p>
	 * To get the projectile from the crossbow, call {@link CrossbowItem#hasProjectile(ItemStack, Item)} passing in {@code stack} and the {@link Item} for the projectile. <p>
	 * The default implementation follows the vanilla values for projectiles
	 *
	 * @param stack  the ItemStack for the crossbow
	 * @param entity the Entity shooting the crossbow
	 * @return the speed of the projectile
	 */
	default float getProjectileSpeed(ItemStack stack, LivingEntity entity) {
		return CrossbowItemAccessor.quilt$getSpeed(stack);
	}
}
