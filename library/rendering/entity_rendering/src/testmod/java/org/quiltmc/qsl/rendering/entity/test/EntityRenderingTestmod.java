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

import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorItem.ArmorSlot;
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

	public static final ArmorItem QUILT_HELMET = new ArmorItem(QuiltArmorMaterial.INSTANCE, ArmorSlot.HELMET, new Item.Settings());
	public static final ArmorItem QUILT_CHESTPLATE = new ArmorItem(QuiltArmorMaterial.INSTANCE, ArmorSlot.CHESTPLATE, new Item.Settings());
	public static final ArmorItem QUILT_LEGGINGS = new ArmorItem(QuiltArmorMaterial.INSTANCE, ArmorSlot.LEGGINGS, new Item.Settings());
	public static final ArmorItem QUILT_BOOTS = new ArmorItem(QuiltArmorMaterial.INSTANCE, ArmorSlot.BOOTS, new Item.Settings());

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registries.ITEM, id("quilt_helmet"), QUILT_HELMET);
		Registry.register(Registries.ITEM, id("quilt_chestplate"), QUILT_CHESTPLATE);
		Registry.register(Registries.ITEM, id("quilt_leggings"), QUILT_LEGGINGS);
		Registry.register(Registries.ITEM, id("quilt_boots"), QUILT_BOOTS);
	}
}
