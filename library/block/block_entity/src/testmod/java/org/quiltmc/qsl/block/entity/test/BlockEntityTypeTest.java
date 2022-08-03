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

package org.quiltmc.qsl.block.entity.test;

import net.minecraft.block.Block;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.block.entity.api.QuiltBlockEntityTypeBuilder;

public class BlockEntityTypeTest implements ModInitializer {
	public static final String NAMESPACE = "quilt_block_entity_testmod";

	public static final AngyBlock INITIAL_ANGY_BLOCK = register(id("initial_angy_block"), new AngyBlock(MapColor.PINK));
	public static final AngyBlock BUILDER_ADDED_ANGY_BLOCK = register(id("builder_added_angy_block"), new AngyBlock(MapColor.BLUE));
	public static final AngyBlock BUILDER_MULTI_1_ANGY_BLOCK = register(id("builder_multi_1_angy_block"), new AngyBlock(MapColor.GREEN));
	public static final AngyBlock BUILDER_MULTI_2_ANGY_BLOCK = register(id("builder_multi_2_angy_block"), new AngyBlock(MapColor.DARK_GREEN));
	public static final AngyBlock POST_ADDED_ANGY_BLOCK = register(id("post_added_angy_block"), new AngyBlock(MapColor.CYAN));
	public static final AngyBlock POST_MULTI_1_ANGY_BLOCK = register(id("post_multi_1_angy_block"), new AngyBlock(MapColor.LIME));
	public static final AngyBlock POST_MULTI_2_ANGY_BLOCK = register(id("post_multi_2_angy_block"), new AngyBlock(MapColor.TERRACOTTA_LIME));

	public static final BlockEntityType<ColorfulBlockEntity> COLORFUL_BLOCK_ENTITY_TYPE = QuiltBlockEntityTypeBuilder.create(
					ColorfulBlockEntity::new, INITIAL_ANGY_BLOCK
			)
			.addBlock(BUILDER_ADDED_ANGY_BLOCK)
			.addBlocks(BUILDER_MULTI_1_ANGY_BLOCK, BUILDER_MULTI_2_ANGY_BLOCK)
			.build();

	@Override
	public void onInitialize(ModContainer mod) {
		Registry.register(Registry.BLOCK_ENTITY_TYPE, id("colorful"), COLORFUL_BLOCK_ENTITY_TYPE);

		COLORFUL_BLOCK_ENTITY_TYPE.addSupportedBlock(POST_ADDED_ANGY_BLOCK);
		COLORFUL_BLOCK_ENTITY_TYPE.addSupportedBlocks(POST_MULTI_1_ANGY_BLOCK, POST_MULTI_2_ANGY_BLOCK);
	}

	private static Identifier id(String path) {
		return new Identifier(NAMESPACE, path);
	}

	private static <B extends Block> B register(Identifier id, B block) {
		Registry.register(Registry.BLOCK, id, block);

		var item = new BlockItem(block, new Item.Settings().group(ItemGroup.MISC));
		Registry.register(Registry.ITEM, id, item);

		return block;
	}
}
