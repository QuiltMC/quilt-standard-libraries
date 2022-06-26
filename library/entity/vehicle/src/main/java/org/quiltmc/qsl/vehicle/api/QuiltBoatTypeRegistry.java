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

package org.quiltmc.qsl.vehicle.api;

import net.minecraft.block.Block;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import org.quiltmc.qsl.vehicle.impl.QuiltBoatTypeRegistryInternals;

/**
 * Allows registering custom {@link net.minecraft.entity.vehicle.BoatEntity.Type}s.
 *
 * <p>It is recommended to also register its associated {@link Item}.
 * Any {@link net.minecraft.entity.vehicle.BoatEntity.Type}s without an associated {@link Item} will by default drop
 * {@link Items#OAK_BOAT}.</p>
 */
public class QuiltBoatTypeRegistry {

	/**
	 * Registers a new {@link BoatEntity.Type}.
	 *
	 * @param baseBlock The {@link Block} dropped by this boat when it breaks from fall damage
	 * @param name the name of the {@link BoatEntity.Type}
	 * @return The created {@link BoatEntity.Type}
	 */
	public static BoatEntity.Type register(Block baseBlock, String name) {
		return QuiltBoatTypeRegistryInternals.createBoatType(baseBlock, name);
	}

	/**
	 * Registers the {@link Item} form of a {@link BoatEntity}.
	 *
	 * @param boatType The {@link BoatEntity.Type} to register this item for
	 * @param item The {@link Item} being registered
	 * @return the {@link Item}
	 * @see BoatEntity#asItem()
	 */
	public static Item registerBoatItem(BoatEntity.Type boatType, Item item) {
		return QuiltBoatTypeRegistryInternals.registerBoatItem(boatType, item);
	}
}
