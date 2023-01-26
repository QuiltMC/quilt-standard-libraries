/*
 * Copyright 2021-2023 QuiltMC
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

package org.quiltmc.qsl.rendering.entity.test;

import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public final class EntityRenderingTestmod implements ModInitializer {
	public static final String NAMESPACE = "quilt_entity_rendering_testmod";

	public static Identifier id(String path) {
		return new Identifier(NAMESPACE, path);
	}

	public static final ArmorItem QUILT_HELMET = Registry.register(Registries.ITEM, id("quilt_helmet"),
			new ArmorItem(QuiltArmorMaterial.INSTANCE, EquipmentSlot.HEAD, new Item.Settings()));
	public static final ArmorItem QUILT_CHESTPLATE = Registry.register(Registries.ITEM, id("quilt_chestplate"),
			new ArmorItem(QuiltArmorMaterial.INSTANCE, EquipmentSlot.CHEST, new Item.Settings()));
	public static final ArmorItem QUILT_LEGGINGS = Registry.register(Registries.ITEM, id("quilt_leggings"),
			new ArmorItem(QuiltArmorMaterial.INSTANCE, EquipmentSlot.LEGS, new Item.Settings()));
	public static final ArmorItem QUILT_BOOTS = Registry.register(Registries.ITEM, id("quilt_boots"),
			new ArmorItem(QuiltArmorMaterial.INSTANCE, EquipmentSlot.FEET, new Item.Settings()));

	@Override
	public void onInitialize(ModContainer mod) {}
}
