/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.tool_attributes.test.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tag.Tag;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.tool_attributes.api.DynamicAttributeTool;

import java.util.UUID;

public class TestDynamicToolItem extends Item implements DynamicAttributeTool {
	public static final UUID TEST_UUID = UUID.fromString("CB3F55D3-645C-4F38-A497-9C13A33DB5CF");

	public TestDynamicToolItem(Settings settings) {
		super(settings);
	}

	@Override
	public int getMiningLevel(Tag<Item> tag, BlockState state, ItemStack stack, @Nullable LivingEntity user) {
		return 0;
	}

	@Override
	public float getMiningSpeedMultiplier(Tag<Item> tag, BlockState state, ItemStack stack, @Nullable LivingEntity user) {
		return 0;
	}

	@Override
	public Multimap<EntityAttribute, EntityAttributeModifier> getDynamicModifiers(EquipmentSlot slot, ItemStack stack, @Nullable LivingEntity user) {
		if (slot.equals(EquipmentSlot.MAINHAND)) {
			ImmutableMultimap.Builder<EntityAttribute, EntityAttributeModifier> builder = ImmutableMultimap.builder();
			builder.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(TEST_UUID, "TEST", -1f, EntityAttributeModifier.Operation.ADDITION));
			builder.put(EntityAttributes.GENERIC_MOVEMENT_SPEED, new EntityAttributeModifier(TEST_UUID, "TEST", -1f, EntityAttributeModifier.Operation.ADDITION));
			return builder.build();
		} else {
			return EMPTY;
		}
	}
}
