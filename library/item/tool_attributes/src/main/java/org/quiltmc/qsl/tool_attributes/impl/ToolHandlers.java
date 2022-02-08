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

package org.quiltmc.qsl.tool_attributes.impl;

import net.fabricmc.api.ModInitializer;
import net.minecraft.item.Items;
import net.minecraft.tag.BlockTags;
import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.mining_levels.api.QuiltMineableTags;
import org.quiltmc.qsl.tool_attributes.api.QuiltToolTags;
import org.quiltmc.qsl.tool_attributes.impl.handlers.ModdedToolsVanillaBlocksToolHandler;
import org.quiltmc.qsl.tool_attributes.impl.handlers.ModdedToolsModdedBlocksToolHandler;
import org.quiltmc.qsl.tool_attributes.impl.handlers.TaggedToolsTaggedBlocksToolHandler;
import org.quiltmc.qsl.tool_attributes.impl.handlers.TaggedToolsModdedBlocksToolHandler;
import org.quiltmc.qsl.tool_attributes.impl.handlers.VanillaToolsModdedBlocksToolHandler;
import org.quiltmc.qsl.tool_attributes.impl.handlers.ShearsVanillaBlocksToolHandler;

import java.util.Arrays;

@ApiStatus.Internal
public class ToolHandlers implements ModInitializer {
	@Override
	public void onInitialize() {
		ToolManagerImpl.general().register(new ModdedToolsModdedBlocksToolHandler());
		ToolManagerImpl.general().register(new VanillaToolsModdedBlocksToolHandler());
		ToolManagerImpl.general().register(new TaggedToolsModdedBlocksToolHandler());
		ToolManagerImpl.tag(QuiltToolTags.PICKAXES).register(new ModdedToolsVanillaBlocksToolHandler(
				Arrays.asList(
						Items.WOODEN_PICKAXE,
						Items.STONE_PICKAXE,
						Items.IRON_PICKAXE,
						Items.DIAMOND_PICKAXE,
						Items.NETHERITE_PICKAXE
				)
		));
		ToolManagerImpl.tag(QuiltToolTags.AXES).register(new ModdedToolsVanillaBlocksToolHandler(
				Arrays.asList(
						Items.WOODEN_AXE,
						Items.STONE_AXE,
						Items.IRON_AXE,
						Items.DIAMOND_AXE,
						Items.NETHERITE_AXE
				)
		));
		ToolManagerImpl.tag(QuiltToolTags.SHOVELS).register(new ModdedToolsVanillaBlocksToolHandler(
				Arrays.asList(
						Items.WOODEN_SHOVEL,
						Items.STONE_SHOVEL,
						Items.IRON_SHOVEL,
						Items.DIAMOND_SHOVEL,
						Items.NETHERITE_SHOVEL
				)
		));
		ToolManagerImpl.tag(QuiltToolTags.HOES).register(new ModdedToolsVanillaBlocksToolHandler(
				Arrays.asList(
						Items.WOODEN_HOE,
						Items.STONE_HOE,
						Items.IRON_HOE,
						Items.DIAMOND_HOE,
						Items.NETHERITE_HOE
				)
		));
		ToolManagerImpl.tag(QuiltToolTags.SWORDS).register(new ModdedToolsVanillaBlocksToolHandler(
				Arrays.asList(
						Items.WOODEN_SWORD,
						Items.STONE_SWORD,
						Items.IRON_SWORD,
						Items.DIAMOND_SWORD,
						Items.NETHERITE_SWORD
				)
		));
		ToolManagerImpl.tag(QuiltToolTags.SHEARS).register(new ShearsVanillaBlocksToolHandler());
		ToolManagerImpl.tag(QuiltToolTags.AXES).register(new TaggedToolsTaggedBlocksToolHandler(BlockTags.AXE_MINEABLE));
		ToolManagerImpl.tag(QuiltToolTags.HOES).register(new TaggedToolsTaggedBlocksToolHandler(BlockTags.HOE_MINEABLE));
		ToolManagerImpl.tag(QuiltToolTags.PICKAXES).register(new TaggedToolsTaggedBlocksToolHandler(BlockTags.PICKAXE_MINEABLE));
		ToolManagerImpl.tag(QuiltToolTags.SHEARS).register(new TaggedToolsTaggedBlocksToolHandler(QuiltMineableTags.SHEARS_MINEABLE));
		ToolManagerImpl.tag(QuiltToolTags.SHOVELS).register(new TaggedToolsTaggedBlocksToolHandler(BlockTags.SHOVEL_MINEABLE));
		ToolManagerImpl.tag(QuiltToolTags.SWORDS).register(new TaggedToolsTaggedBlocksToolHandler(QuiltMineableTags.SWORD_MINEABLE));
	}
}
