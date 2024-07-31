/*
 * Copyright 2021 The Quilt Project
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

import java.util.EnumMap;
import java.util.List;

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorItem.ArmorSlot;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.Item;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.Holder;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public final class EntityRenderingTestmod implements ModInitializer {
	public static final String NAMESPACE = "quilt_entity_rendering_testmod";

	public static Identifier id(String path) {
		return Identifier.of(NAMESPACE, path);
	}

	private static final Holder<ArmorMaterial> QUILT_ARMOR_MATERIAL = Registry.registerHolder(
			Registries.ARMOR_MATERIAL,
			id("armor_material"),
			new ArmorMaterial(
				Util.make(new EnumMap<>(ArmorItem.ArmorSlot.class), (map) -> {
					map.put(ArmorSlot.BOOTS, 3);
					map.put(ArmorSlot.LEGGINGS, 6);
					map.put(ArmorSlot.CHESTPLATE, 8);
					map.put(ArmorSlot.HELMET, 3);
					map.put(ArmorSlot.BODY, 9);
				}),
				25,
				SoundEvents.ITEM_ARMOR_EQUIP_TURTLE,
					() -> Ingredient.ofTag(ItemTags.WOOL),
				List.of(new ArmorMaterial.Layer(id("armor_material"))),
				4.0F, 0.15F)
	);

	public static final ArmorItem QUILT_HELMET = new ArmorItem(QUILT_ARMOR_MATERIAL, ArmorSlot.HELMET, new Item.Settings());
	public static final ArmorItem QUILT_CHESTPLATE = new ArmorItem(QUILT_ARMOR_MATERIAL, ArmorSlot.CHESTPLATE, new Item.Settings());
	public static final ArmorItem QUILT_LEGGINGS = new ArmorItem(QUILT_ARMOR_MATERIAL, ArmorSlot.LEGGINGS, new Item.Settings());
	public static final ArmorItem QUILT_BOOTS = new ArmorItem(QUILT_ARMOR_MATERIAL, ArmorSlot.BOOTS, new Item.Settings());

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registries.ITEM, id("quilt_helmet"), QUILT_HELMET);
		Registry.register(Registries.ITEM, id("quilt_chestplate"), QUILT_CHESTPLATE);
		Registry.register(Registries.ITEM, id("quilt_leggings"), QUILT_LEGGINGS);
		Registry.register(Registries.ITEM, id("quilt_boots"), QUILT_BOOTS);
	}
}
