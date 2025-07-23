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

import static org.quiltmc.qsl.item.extensions.test.ItemExtensionTestUtil.createItemKey;

import org.jetbrains.annotations.NotNull;

import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.TridentEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.world.ServerWorld;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.extensions.api.bow.BowShotProjectileEvents;
import org.quiltmc.qsl.item.extensions.api.bow.ProjectileModifyingBowItem;
import org.quiltmc.qsl.item.extensions.api.crossbow.ProjectileModifyingCrossbowItem;

public class BowsTest implements ModInitializer {
	private static final RegistryKey<Item> TEST_BOW_KEY = createItemKey("test_bow");

	private static final RegistryKey<Item> TEST_CROSSBOW_KEY = createItemKey("test_crossbow");

	public static final Item TEST_BOW = new ProjectileModifyingBowItem(
		new Item.Settings()
			.key(TEST_BOW_KEY)
			.maxCount(1)
	) {
		@Override
		public void onProjectileShot(ItemStack bowStack, ItemStack arrowStack, LivingEntity user, float pullProgress, PersistentProjectileEntity projectile) {
			projectile.setCritical(true);
		}
	};

	public static final Item TEST_CROSSBOW = new ProjectileModifyingCrossbowItem(
		new Item.Settings()
			.key(TEST_CROSSBOW_KEY)
			.maxCount(1)
	) {
		@Override
		public void onProjectileShot(
				ItemStack crossbowStack, ItemStack projectileStack,
				LivingEntity entity, @NotNull ProjectileEntity projectileEntity
		) {
			if (projectileEntity instanceof PersistentProjectileEntity persistentProjectileEntity) {
				persistentProjectileEntity.setDamage(1000);
			}
		}

		@Override
		public float getProjectileSpeed(
				@NotNull ItemStack stack, @NotNull ChargedProjectilesComponent component, @NotNull LivingEntity entity
		) {
			return 10f;
		}
	};

	private static PersistentProjectileEntity replaceIllusionerArrowsWithTridents(
			ItemStack bowStack, ItemStack arrowStack, LivingEntity user, float pullProgress,
			PersistentProjectileEntity projectile
	) {
		if (user instanceof IllusionerEntity && user.getWorld() instanceof ServerWorld world) {
			return ProjectileEntity.spawn(TridentEntity::new, world, new ItemStack(Items.TRIDENT), user, 0, 1.5f, 1.0f);
		} else {
			return projectile;
		}
	}

	private static void makeSkeletonArrowsNoClip(
			ItemStack bowStack, ItemStack arrowStack, LivingEntity user, float pullProgress,
			PersistentProjectileEntity projectile
	) {
		if (user instanceof AbstractSkeletonEntity) {
			projectile.setNoClip(true);
		}
	}

	@Override
	public void onInitialize(ModContainer mod) {
		// Registers a custom bow.
		Registry.register(Registries.ITEM, TEST_BOW_KEY, TEST_BOW);
		// Registers a custom crossbow.
		Registry.register(Registries.ITEM, TEST_CROSSBOW_KEY, TEST_CROSSBOW);

		BowShotProjectileEvents.BOW_MODIFY_SHOT_PROJECTILE.register(BowsTest::makeSkeletonArrowsNoClip);

		BowShotProjectileEvents.BOW_REPLACE_SHOT_PROJECTILE.register(BowsTest::replaceIllusionerArrowsWithTridents);
	}
}
