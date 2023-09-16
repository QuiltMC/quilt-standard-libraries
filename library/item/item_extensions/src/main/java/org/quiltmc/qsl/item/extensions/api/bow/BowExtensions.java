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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

import net.minecraft.item.BowItem;
import net.minecraft.item.ItemStack;

import org.quiltmc.qsl.base.api.util.InjectedInterface;

/**
 * An interface to implement for all custom bows in Quilt.
 * <p>
 * This is meant to be used on a {@link net.minecraft.item.BowItem} class.
 * Unless similar functionality is implemented on your custom item, most functionality will not work.
 * <p>
 * In order to modify the projectile shot from your bow, implementing and registering {@link BowShotProjectileEvents.ModifyProjectileFromBow} is recommended.
 *
 * @see ProjectileModifyingBowItem
 */
@InjectedInterface(BowItem.class)
public interface BowExtensions {
	/**
	 * {@return the draw progress of the bow between {@code 0} and {@code 1}}
	 *
	 * @param useTicks the number of ticks the bow has been drawn
	 * @param bowStack the item stack for the bow
	 */
	default @Range(from = 0, to = 1) float getCustomPullProgress(int useTicks, @NotNull ItemStack bowStack) {
		return BowItem.getPullProgress(useTicks);
	}
}
