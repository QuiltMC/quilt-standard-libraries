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

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.ShearsItem;
import net.minecraft.tag.Tag;
import net.minecraft.util.ActionResult;
import net.minecraft.util.TypedActionResult;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.tool_attributes.api.DynamicAttributeTool;
import org.quiltmc.qsl.tool_attributes.impl.ToolManagerImpl;

@ApiStatus.Internal
public class ShearsVanillaBlocksToolHandler implements ToolManagerImpl.ToolHandler {
	private final Item vanillaItem = Items.SHEARS;

	@NotNull
	@Override
	public ActionResult isEffectiveOn(Tag<Item> tag, BlockState state, ItemStack stack, LivingEntity user) {
		if (ToolManagerImpl.entryNullable(state.getBlock()) != null) {
			// Block is a modded block, and we should ignore it
			return ActionResult.PASS;
		}

		if (!(stack.getItem() instanceof DynamicAttributeTool)) {
			if (!(stack.getItem() instanceof ShearsItem)) {
				return vanillaItem.isSuitableFor(state) ? ActionResult.SUCCESS : ActionResult.PASS;
			} else {
				return stack.getItem().isSuitableFor(state) ? ActionResult.SUCCESS : ActionResult.PASS;
			}
		}

		return ActionResult.PASS;
	}

	@NotNull
	@Override
	public TypedActionResult<Float> getMiningSpeedMultiplier(Tag<Item> tag, BlockState state, ItemStack stack, LivingEntity user) {
		float speed = 1f;

		if (!(stack.getItem() instanceof DynamicAttributeTool)) {
			if (!(stack.getItem() instanceof ShearsItem)) {
				speed = vanillaItem.getMiningSpeedMultiplier(new ItemStack(vanillaItem), state);
			} else {
				speed = stack.getItem().getMiningSpeedMultiplier(stack, state);
			}
		}

		return speed != 1f ? TypedActionResult.success(speed) : TypedActionResult.pass(1f);
	}
}
