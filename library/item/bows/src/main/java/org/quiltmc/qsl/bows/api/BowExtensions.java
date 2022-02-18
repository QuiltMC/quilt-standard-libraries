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

import net.minecraft.item.ItemStack;

/**
 * An interface to implement for all custom bows in Quilt. <br>
 * This is meant to be used on a {@link net.minecraft.item.BowItem} class. Unless similar functionality is implemented on your custom item, most functionality will not work. <br>
 * In order to modify the projectile shot from your bow, implementing and registering {@link ShotProjectileEvents.ModifyProjectileFromBow} is recommended.
 *
 * @see ExtendedBowItem
 */
public interface BowExtensions {
	/**
	 * Returns the draw progress of the bow between 0 and 1.
	 *
	 * @param useTicks the number of ticks the bow has been drawn.
	 * @param bowStack the ItemStack for the bow
	 * @return the progress of the pull from {@code 0.0f} to {@code 1.0f}.
	 */
	default float getCustomPullProgress(int useTicks, ItemStack bowStack) {
		return net.minecraft.item.BowItem.getPullProgress(useTicks);
	}
}
