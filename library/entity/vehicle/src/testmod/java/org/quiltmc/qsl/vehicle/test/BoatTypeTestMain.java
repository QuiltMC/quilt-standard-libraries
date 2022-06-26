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

package org.quiltmc.qsl.vehicle.test;

import net.minecraft.block.Blocks;
import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.vehicle.api.QuiltBoatTypeRegistry;

public class BoatTypeTestMain implements ModInitializer {

	@Override
	public void onInitialize(ModContainer mod) {
		var type = QuiltBoatTypeRegistry.register(Blocks.BEDROCK, "quilt");
		QuiltBoatTypeRegistry.registerBoatItem(type, Registry.register(Registry.ITEM, new Identifier("quilt_vehicle_testmod", "quilt_boat"), new BoatItem(type, new Item.Settings().group(ItemGroup.TRANSPORTATION))));
	}
}
