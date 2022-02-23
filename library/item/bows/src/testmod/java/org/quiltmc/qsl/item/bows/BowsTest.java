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

package org.quiltmc.qsl.item.bows;

import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.bows.api.ExtendedBowItem;
import org.quiltmc.qsl.item.bows.api.ShotProjectileEvents;
import org.quiltmc.qsl.item.bows.api.ExtendedCrossbowItem;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import net.fabricmc.loader.api.ModContainer;

public class BowsTest implements ModInitializer {
	public static final Item TEST_BOW = new ExtendedBowItem(new Item.Settings().group(ItemGroup.COMBAT)) {
		@Override
		public void onProjectileShot(ItemStack bowStack, ItemStack arrowStack, LivingEntity user, float pullProgress, PersistentProjectileEntity persistentProjectileEntity) {
			persistentProjectileEntity.setPunch(100);
		}
	};

	public static final Item TEST_CROSSBOW = new ExtendedCrossbowItem(new Item.Settings().group(ItemGroup.COMBAT)) {
		@Override
		public void onProjectileShot(ItemStack crossbowStack, ItemStack projectileStack, LivingEntity entity, @NotNull PersistentProjectileEntity persistentProjectileEntity) {
			persistentProjectileEntity.setDamage(1000);
		}

		@Override
		public float getProjectileSpeed(ItemStack stack, LivingEntity entity) {
			return 10f;
		}
	};
	public static final String MOD_ID = "quilt_bows_testmod";

	@Override
	public void onInitialize(ModContainer container) {
		// Registers a custom bow.
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "test_bow"), TEST_BOW);
		ShotProjectileEvents.BOW_MODIFY_SHOT_PROJECTILE.register((ShotProjectileEvents.ModifyProjectileFromBow) TEST_BOW);
		// Registers a custom crossbow.
		Registry.register(Registry.ITEM, new Identifier(MOD_ID, "test_crossbow"), TEST_CROSSBOW);
		ShotProjectileEvents.CROSSBOW_MODIFY_SHOT_PROJECTILE.register((ShotProjectileEvents.ModifyProjectileFromCrossbow) TEST_CROSSBOW);
	}
}
