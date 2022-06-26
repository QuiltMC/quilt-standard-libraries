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

package org.quiltmc.qsl.vehicle.impl;

import net.minecraft.block.Block;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.Item;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@ApiStatus.Internal
public final class QuiltBoatTypeRegistryInternals {
	private static Logger LOGGER = LoggerFactory.getLogger("quilt_vehicle");
	private static BoatTypeExtender EXTENDER = null;
	private static final Map<BoatEntity.Type, Item> ITEMS = new HashMap<>();

	public static void register(BoatTypeExtender extender) {
		Objects.requireNonNull(extender, "extender can't be null");
		EXTENDER = extender;
	}

	public static BoatEntity.Type createBoatType(Block baseBlock, String name) {
		Objects.requireNonNull(EXTENDER, "cannot attempt to modify boat types before mixins apply!");
		String internalName = name.toUpperCase(Locale.ROOT);

		return EXTENDER.create(internalName, baseBlock, name);
	}

	public static Item registerBoatItem(BoatEntity.Type type, Item item) {
		if (ITEMS.put(type, item) != null) {
			LOGGER.debug("Overriding existing item for boat type {}", type.getName());
		}
		return item;
	}

	public static Optional<Item> getBoatItem(BoatEntity.Type type) {
		return Optional.ofNullable(ITEMS.get(type));
	}

	@FunctionalInterface
	public interface BoatTypeExtender {
		BoatEntity.Type create(String internalName, Block baseBlock, String name);
	}
}
