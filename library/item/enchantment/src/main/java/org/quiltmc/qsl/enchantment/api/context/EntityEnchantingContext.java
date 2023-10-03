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

package org.quiltmc.qsl.enchantment.api.context;

import org.jetbrains.annotations.Contract;

import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.random.RandomGenerator;
import net.minecraft.world.World;

/**
 * A class that contains contextual information about the enchantment process.
 * <p>
 * Contains further information about the entity performing the enchanting.
 */
public class EntityEnchantingContext<E extends Entity> extends WorldEnchantingContext {
	protected final E entity;

	public EntityEnchantingContext(int level, int power, ItemStack stack, World world, RandomGenerator random, boolean treasureAllowed, E entity) {
		super(level, power, stack, world, random, treasureAllowed);
		this.entity = entity;
	}

	@Override
	@Contract("_->new")
	public EntityEnchantingContext<E> withLevel(int level) {
		return new EntityEnchantingContext<>(level, this.power, this.stack, this.world, this.random, this.treasureAllowed, this.entity);
	}

	@Override
	@Contract("_->new")
	public EntityEnchantingContext<E> withPower(int power) {
		return new EntityEnchantingContext<>(this.level, power, this.stack, this.world, this.random, this.treasureAllowed, this.entity);
	}

	@Override
	@Contract("_,_,_->new")
	public EnchantingContext withCoreContext(ItemStack stack, RandomGenerator random, boolean treasureAllowed) {
		return new EntityEnchantingContext<>(this.level, this.power, stack, this.world, random, treasureAllowed, this.entity);
	}

	/**
	 * @return the entity performing the enchanting
	 */
	public E getEntity() {
		return this.entity;
	}
}
