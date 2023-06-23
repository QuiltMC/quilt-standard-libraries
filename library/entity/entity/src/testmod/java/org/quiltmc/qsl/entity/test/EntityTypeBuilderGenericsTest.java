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

package org.quiltmc.qsl.entity.test;

import java.util.Collections;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SpawnGroup;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.world.World;

import org.quiltmc.qsl.entity.api.QuiltEntityTypeBuilder;

// This test is intentionally not an entrypoint to verify the generics of the entity type builder propagate properly
final class EntityTypeBuilderGenericsTest {
	static EntityType<Entity> ENTITY_1 = QuiltEntityTypeBuilder.create().build();
	static EntityType<LivingEntity> LIVING_ENTITY_1 = QuiltEntityTypeBuilder.createLiving().build();
	static EntityType<TestEntity> TEST_ENTITY_1 = QuiltEntityTypeBuilder.createLiving()
			.entityFactory(TestEntity::new)
			.spawnGroup(SpawnGroup.CREATURE)
			.build();
	static EntityType<TestEntity> OLD_TEST = QuiltEntityTypeBuilder.<TestEntity>createLiving()
			.entityFactory(TestEntity::new)
			.spawnGroup(SpawnGroup.CREATURE)
			.build();
	static EntityType<TestMob> OLD_MOB = QuiltEntityTypeBuilder.<TestMob>createMob()
			.disableSaving()
			.entityFactory(TestMob::new)
			.build();
	static EntityType<TestMob> MOB_TEST = QuiltEntityTypeBuilder.createMob()
			.disableSaving()
			.entityFactory(TestMob::new)
			.build();

	private static class TestEntity extends LivingEntity {
		protected TestEntity(EntityType<? extends LivingEntity> entityType, World world) {
			super(entityType, world);
		}

		@Override
		public Iterable<ItemStack> getArmorItems() {
			return Collections.emptyList();
		}

		@Override
		public ItemStack getEquippedStack(EquipmentSlot slot) {
			return ItemStack.EMPTY;
		}

		@Override
		public void equipStack(EquipmentSlot slot, ItemStack stack) {}

		@Override
		public Arm getMainArm() {
			return Arm.RIGHT;
		}
	}

	private static class TestMob extends MobEntity {
		protected TestMob(EntityType<? extends MobEntity> entityType, World world) {
			super(entityType, world);
		}
	}
}
