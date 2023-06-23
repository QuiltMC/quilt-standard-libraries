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

package org.quiltmc.qsl.item.extensions.test;

import org.jetbrains.annotations.NotNull;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.extensions.api.bow.ProjectileModifyingBowItem;
import org.quiltmc.qsl.item.extensions.api.crossbow.ProjectileModifyingCrossbowItem;

public class BowsTest implements ModInitializer {
	public static final Item TEST_BOW = new ProjectileModifyingBowItem(new Item.Settings().maxCount(1)) {
		@Override
		public void onProjectileShot(ItemStack bowStack, ItemStack arrowStack, LivingEntity user, float pullProgress, PersistentProjectileEntity projectile) {
			projectile.setPunch(100);
		}
	};

	public static final Item TEST_CROSSBOW = new ProjectileModifyingCrossbowItem(new Item.Settings().maxCount(1)) {
		@Override
		public void onProjectileShot(ItemStack crossbowStack, ItemStack projectileStack, LivingEntity entity, @NotNull PersistentProjectileEntity persistentProjectileEntity) {
			persistentProjectileEntity.setDamage(1000);
		}

		@Override
		public float getProjectileSpeed(@NotNull ItemStack stack, @NotNull LivingEntity entity) {
			return 10f;
		}
	};

	@Override
	public void onInitialize(ModContainer mod) {
		// Registers a custom bow.
		Registry.register(Registries.ITEM, new Identifier(mod.metadata().id(), "test_bow"), TEST_BOW);
		// Registers a custom crossbow.
		Registry.register(Registries.ITEM, new Identifier(mod.metadata().id(), "test_crossbow"), TEST_CROSSBOW);
	}
}
