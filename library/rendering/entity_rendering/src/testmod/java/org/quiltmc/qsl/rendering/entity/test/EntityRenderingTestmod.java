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

import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ArmorType;
import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.ItemTags;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.EquipmentAsset;
import net.minecraft.util.EquipmentAssets;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public final class EntityRenderingTestmod implements ModInitializer {
	public static final String NAMESPACE = "quilt_entity_rendering_testmod";

	public static final RegistryKey<Item> QUILT_HELMET_KEY = createItemKey("quilt_helmet");
	public static final RegistryKey<Item> QUILT_CHESTPLATE_KEY = createItemKey("quilt_chestplate");
	public static final RegistryKey<Item> QUILT_LEGGINGS_KEY = createItemKey("quilt_leggings");
	public static final RegistryKey<Item> QUILT_BOOTS_KEY = createItemKey("quilt_boots");

	private static final ArmorMaterial QUILT_ARMOR_MATERIAL =
			new ArmorMaterial(
				3,
				Util.make(new EnumMap<>(ArmorType.class), (map) -> {
					map.put(ArmorType.BOOTS, 3);
					map.put(ArmorType.LEGGINGS, 6);
					map.put(ArmorType.CHESTPLATE, 8);
					map.put(ArmorType.HELMET, 3);
					map.put(ArmorType.BODY, 9);
				}),
				25,
				SoundEvents.ITEM_ARMOR_EQUIP_TURTLE,
				4.0F, 0.15F,
				ItemTags.WOOL,
				createAssetKey("quilt")
			);

	public static final Item QUILT_HELMET = new Item(createQuiltArmorSettings(QUILT_HELMET_KEY, ArmorType.HELMET));
	public static final Item QUILT_CHESTPLATE = new Item(createQuiltArmorSettings(QUILT_CHESTPLATE_KEY, ArmorType.CHESTPLATE));
	public static final Item QUILT_LEGGINGS = new Item(createQuiltArmorSettings(QUILT_LEGGINGS_KEY, ArmorType.LEGGINGS));
	public static final Item QUILT_BOOTS = new Item(createQuiltArmorSettings(QUILT_BOOTS_KEY, ArmorType.BOOTS));

	public static Identifier createId(String path) {
		return Identifier.of(NAMESPACE, path);
	}

	public static RegistryKey<EquipmentAsset> createAssetKey(String path) {
		return RegistryKey.of(EquipmentAssets.REGISTRY, createId(path));
	}

	private static RegistryKey<Item> createItemKey(String path) {
		return RegistryKey.of(RegistryKeys.ITEM, createId(path));
	}

	private static Item.Settings createQuiltArmorSettings(RegistryKey<Item> key, ArmorType type) {
		// method_66332 is armor
		return new Item.Settings().key(key).method_66332(QUILT_ARMOR_MATERIAL, type);
	}

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registries.ITEM, QUILT_HELMET_KEY, QUILT_HELMET);
		Registry.register(Registries.ITEM, QUILT_CHESTPLATE_KEY, QUILT_CHESTPLATE);
		Registry.register(Registries.ITEM, QUILT_LEGGINGS_KEY, QUILT_LEGGINGS);
		Registry.register(Registries.ITEM, QUILT_BOOTS_KEY, QUILT_BOOTS);
	}
}
