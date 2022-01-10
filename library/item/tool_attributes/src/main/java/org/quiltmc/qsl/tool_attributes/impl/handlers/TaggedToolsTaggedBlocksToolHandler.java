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

package org.quiltmc.qsl.tool_attributes.impl.handlers;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ToolItem;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.tool_attributes.api.DynamicAttributeTool;
import org.quiltmc.qsl.tool_attributes.impl.ToolManagerImpl;

/**
 * This handler handles items that are registered in a tool tag,
 * but aren't any known tool items in code. For that reason, we use a few callback values:
 * The mining level of this kind of item is always 0, and the mining speed multiplier is always 1.
 *
 * <p>Only applicable to items that are not a subclass of {@link org.quiltmc.qsl.tool_attributes.api.DynamicAttributeTool} or {@link net.minecraft.item.ToolItem}</p>
 */
@ApiStatus.Internal
public class TaggedToolsTaggedBlocksToolHandler implements ToolManagerImpl.ToolHandler {
	private final Tag<Block> mineableTag;

	public TaggedToolsTaggedBlocksToolHandler(Tag<Block> mineableTag) {
		this.mineableTag = mineableTag;
	}

	@NotNull
	@Override
	public ActionResult isEffectiveOn(Tag<Item> tag, BlockState state, ItemStack stack, @Nullable LivingEntity user) {
		if (!(stack.getItem() instanceof DynamicAttributeTool) && !(stack.getItem() instanceof ToolItem)) {
			if (state.isIn(mineableTag)) {
				return ActionResult.SUCCESS;
			}
		}

		return ActionResult.PASS;
	}
}
