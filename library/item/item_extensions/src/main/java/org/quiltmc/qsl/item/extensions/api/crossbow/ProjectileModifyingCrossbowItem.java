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

package org.quiltmc.qsl.item.extensions.api.crossbow;

import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;

/**
 * This is the default implementation for {@link CrossbowExtensions}, allowing for the easy creation of new crossbows with no new modded functionality.
 * <p>
 * This crossbow automatically registers itself to modify its projectiles with {@link ProjectileModifyingCrossbowItem#onProjectileShot(ItemStack, ItemStack, LivingEntity, PersistentProjectileEntity)}
 */
public class ProjectileModifyingCrossbowItem extends CrossbowItem implements CrossbowShotProjectileEvents.ModifyProjectileFromCrossbow {
	public ProjectileModifyingCrossbowItem(Settings settings) {
		super(settings);
	}

	@Override
	public final void modifyProjectileShot(ItemStack crossbowStack, ItemStack projectileStack, LivingEntity user,
			@NotNull PersistentProjectileEntity projectile) {
		if (crossbowStack.getItem() == this) {
			this.onProjectileShot(crossbowStack, projectileStack, user, projectile);
		}
	}

	public void onProjectileShot(ItemStack crossbowStack, ItemStack arrowStack, LivingEntity user, PersistentProjectileEntity projectile) {}
}
