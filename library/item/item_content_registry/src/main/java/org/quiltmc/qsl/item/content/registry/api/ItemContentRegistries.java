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

package org.quiltmc.qsl.item.content.registry.api;

import com.mojang.serialization.Codec;

import net.minecraft.item.Item;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import org.quiltmc.qsl.registry.attachment.api.RegistryEntryAttachment;

/**
 * Holds {@link RegistryEntryAttachment}s for different properties that items can hold.
 * <p>
 * Current properties:
 * <ul>
 *     <li>{@link #FUEL_TIMES}</li>
 *     <li>{@link #COMPOST_CHANCES}</li>
 * </ul>
 */
public class ItemContentRegistries {
	/**
	 * The namespace for the content registries.
	 */
	public static final String NAMESPACE = "quilt";

	/**
	 * A {@link RegistryEntryAttachment} for how long different items burn in a furnace. The value is stored in ticks.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt/attachments/minecraft/item/fuel_times.json}
	 */
	public static final RegistryEntryAttachment<Item, Integer> FUEL_TIMES = RegistryEntryAttachment
			.builder(Registries.ITEM,
					new Identifier(NAMESPACE, "fuel_times"),
					Integer.class,
					Codec.intRange(0, Integer.MAX_VALUE))
			.build();

	/**
	 * A {@link RegistryEntryAttachment} for the chance that the composter level increases when compositing an item. The value is stored as a value 0 to 1.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt/attachments/minecraft/item/compost_chances.json}
	 */
	public static final RegistryEntryAttachment<Item, Float> COMPOST_CHANCES = RegistryEntryAttachment
			.builder(Registries.ITEM,
					new Identifier(NAMESPACE, "compost_chances"),
					Float.class,
					Codec.floatRange(0, 1))
			.build();

	/**
	 * A {@link RegistryEntryAttachment} for how long different items burn in a brewing stand. The value is the number of brews it can make.
	 * <p>
	 * Values can be set via code and through a data-pack with the file {@code data/quilt/attachments/minecraft/item/brewing_fuels.json}
	 */
	public static final RegistryEntryAttachment<Item, Integer> BREWING_FUELS = RegistryEntryAttachment
			.builder(Registries.ITEM,
					new Identifier(NAMESPACE, "brewing_fuels"),
					Integer.class,
					Codec.intRange(0, Integer.MAX_VALUE))
			.build();
}
