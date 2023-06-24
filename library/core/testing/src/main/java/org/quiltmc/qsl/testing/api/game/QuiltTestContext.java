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

package org.quiltmc.qsl.testing.api.game;

import org.jetbrains.annotations.NotNull;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.test.GameTestState;
import net.minecraft.test.TestContext;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Represents Quilt-provided extensions to {@link TestContext}.
 * <p>
 * This is the class that is passed in tests with default handling.
 */
public class QuiltTestContext extends TestContext {
	public QuiltTestContext(GameTestState test) {
		super(test);
	}

	/**
	 * Expects the given block state at the given block position.
	 *
	 * @param state the expected block state
	 * @param pos   the position to check for
	 */
	public void expectBlockState(@NotNull BlockState state, @NotNull BlockPos pos) {
		this.checkBlockState(pos, s -> s.equals(state), () -> "Expected block state " + state + " at position " + pos.toShortString() + '.');
	}

	/**
	 * Uses the given item stack on the specified position.
	 *
	 * @param player  the player who uses the item
	 * @param stack   the item stack to use
	 * @param pos     the position of the use hit
	 * @param sideHit the side that's being hit for using the item
	 */
	public void useStackOnBlockAt(@NotNull PlayerEntity player, @NotNull ItemStack stack, @NotNull BlockPos pos, @NotNull Direction sideHit) {
		var actualPos = this.getAbsolutePos(pos);
		var blockHitResult = new BlockHitResult(Vec3d.ofCenter(actualPos), sideHit, actualPos, false);
		var itemUsageContext = new ItemUsageContext(player, Hand.MAIN_HAND, blockHitResult);
		stack.useOnBlock(itemUsageContext);
	}
}
