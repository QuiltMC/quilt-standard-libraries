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

package org.quiltmc.qsl.block.entity.test;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AngyBlock extends BlockWithEntity {
	public AngyBlock(MapColor mapColor) {
		super(AbstractBlock.Settings.copy(Blocks.STONE).mapColor(mapColor));
	}

	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		if (!world.isClient()) {
			var blockEntity = BlockEntityTypeTest.COLORFUL_BLOCK_ENTITY_TYPE.get(world, pos);

			if (blockEntity == null) {
				throw new AssertionError("Missing block entity for angy block at " + pos);
			}

			if (player.isSneaking()) {
				blockEntity.rollColor();
				player.sendMessage(Text.literal("Argh! Why did you dance!")
						.styled(style -> style.withColor(0xff000000 | blockEntity.getColor())), false);
			} else {
				player.sendMessage(Text.literal("I'm am angy block!! But I like the color #")
								.append(Integer.toHexString(blockEntity.getColor())).append("!")
								.styled(style -> style.withColor(0xff000000 | blockEntity.getColor())),
						false
				);
			}
		}

		return ActionResult.SUCCESS;
	}

	@Override
	public @Nullable BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return BlockEntityTypeTest.COLORFUL_BLOCK_ENTITY_TYPE.instantiate(pos, state);
	}

	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
}
