/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.item.extensions.test;

import net.minecraft.item.Item;
import net.minecraft.item.MiningToolItem;
import net.minecraft.item.ToolMaterials;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.tag.BlockTags;
import net.minecraft.util.Identifier;
import net.minecraft.util.Rarity;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;

public class TransitiveAccessWidenerTest implements ModInitializer {
	public static final Item MODDED_MINING_TOOL = new MiningToolItem(
			ToolMaterials.DIAMOND,
			BlockTags.CORAL_BLOCKS,
			new Item.Settings().maxCount(1).rarity(Rarity.RARE)
	);

	@Override
	public void onInitialize(ModContainer mod) {
		// Registers a custom mining tool, which is not possible without an access widener.
		Registry.register(Registries.ITEM, Identifier.of(mod.metadata().id(), "modded_mining_tool"), MODDED_MINING_TOOL);
	}
}
