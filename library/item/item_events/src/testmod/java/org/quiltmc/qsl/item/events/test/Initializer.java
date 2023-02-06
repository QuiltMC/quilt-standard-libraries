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

package org.quiltmc.qsl.item.events.test;

import org.jetbrains.annotations.NotNull;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.item.Items;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.ActionResult;

import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.ModInitializer;
import org.quiltmc.qsl.item.events.api.ItemInteractionEvents;

public final class Initializer implements ModInitializer,
		ItemInteractionEvents.UsedOnBlock {
	public static final Initializer INSTANCE = new Initializer();

	private Initializer() {}

	@Override
	public void onInitialize(ModContainer mod) {}

	@Override
	public @NotNull ActionResult onItemUsedOnBlock(@NotNull ItemUsageContext context) {
		if (context.canPlaceOnBlock()
				&& context.getBlockState().isOf(Blocks.DIRT) && context.getStack().isOf(Items.WOODEN_PICKAXE)) {
			context.playSoundAtBlock(SoundEvents.BLOCK_ANVIL_USE, SoundCategory.BLOCKS,
					1.0F, context.getWorldRandom().nextFloat() * 0.1F + 0.9F, true);
			context.replaceBlockState(Blocks.DIAMOND_BLOCK.getDefaultState());
			context.damageStack();
			return context.success();
		}
		return ActionResult.PASS;
	}
}
