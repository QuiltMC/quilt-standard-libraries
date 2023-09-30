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

package org.quiltmc.qsl.item.setting.api;

import java.util.function.Consumer;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

/**
 * Allows an item to run custom logic when {@link ItemStack#damage(int, LivingEntity, Consumer)} is called.
 * This is useful for items that, for example, may drain durability from some other source before damaging
 * the stack itself.
 * <p>
 * Custom damage handlers can be set with {@link QuiltItemSettings#customDamage}.
 */
@FunctionalInterface
public interface CustomDamageHandler {
	/**
	 * Called to apply damage to the given stack.
	 * This can be used to e.g. drain from a battery before actually damaging the item.
	 *
	 * @param stack         the {@link ItemStack} that is being damaged
	 * @param amount        the amount of damage originally requested
	 * @param entity        the {@link LivingEntity} whose item is being damaged
	 * @param breakCallback callback when the stack reaches zero damage. See {@link ItemStack#damage(int, LivingEntity, Consumer)} and its callsites for more information.
	 * @return the amount of damage to pass to vanilla's logic
	 */
	int damage(ItemStack stack, int amount, LivingEntity entity, Consumer<LivingEntity> breakCallback);
}
