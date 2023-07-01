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

package org.quiltmc.qsl.item.extensions.api.bow;

import org.jetbrains.annotations.Range;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;

/**
 * This is the default implementation for {@link BowExtensions}, allowing for the easy creation of new bows with no new modded functionality.
 * <p>
 * This bow automatically registers itself to modify its arrows with {@link ProjectileModifyingBowItem#onProjectileShot(ItemStack, ItemStack, LivingEntity, float, PersistentProjectileEntity)}
 */
public class ProjectileModifyingBowItem extends BowItem implements BowShotProjectileEvents.ModifyProjectileFromBow {
	public ProjectileModifyingBowItem(Settings settings) {
		super(settings);
	}

	@Override
	public final void modifyProjectileShot(ItemStack bowStack, ItemStack arrowStack, LivingEntity user, @Range(from = 0, to = 1) float pullProgress,
			PersistentProjectileEntity projectile) {
		if (bowStack.getItem() == this) {
			this.onProjectileShot(bowStack, arrowStack, user, pullProgress, projectile);
		}
	}

	public void onProjectileShot(ItemStack bowStack, ItemStack arrowStack, LivingEntity user, @Range(from = 0, to = 1) float pullProgress,
			PersistentProjectileEntity projectile) {}
}
