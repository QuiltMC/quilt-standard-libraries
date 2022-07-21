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

package org.quiltmc.qsl.component.test;

import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.Block;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.BlockEntityTicker;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TestBlock extends Block implements BlockEntityProvider {
	public TestBlock(Settings settings) {
		super(settings);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(World world, BlockState state, BlockEntityType<T> type) {
		return TestBlockEntity::tick;
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return ComponentTestMod.TEST_BE_TYPE.instantiate(pos, state);
	}

	@SuppressWarnings("deprecation")
	@Override
	public ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, Hand hand, BlockHitResult hit) {
		BlockEntity be = world.getBlockEntity(pos);
		if (world.isClient || be == null) {
			return super.onUse(state, world, pos, player, hand, hit);
		}

		ItemStack handStack = player.getStackInHand(hand);

		if (!handStack.isEmpty()) {
			final MutableObject<ActionResult> ret = new MutableObject<>(ActionResult.PASS);

			be.expose(ComponentTestMod.CHUNK_INVENTORY).ifJust(inventoryComponent -> {
				var stack = inventoryComponent.getStack(0);

				if (stack.isEmpty()) {
					var copied = handStack.copy();
					copied.setCount(1);
					inventoryComponent.setStack(0, copied);
					inventoryComponent.save();
					inventoryComponent.sync();
					ret.setValue(ActionResult.SUCCESS);
				} else {
					if (ItemStack.canCombine(stack, handStack)) {
						stack.increment(1);
						handStack.decrement(1);
						inventoryComponent.save();
						inventoryComponent.sync();
						ret.setValue(ActionResult.SUCCESS);
					}
				}
			});

			return ret.getValue();
		}

		return super.onUse(state, world, pos, player, hand, hit);
	}

	@SuppressWarnings("deprecation")
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.ENTITYBLOCK_ANIMATED;
	}
}
