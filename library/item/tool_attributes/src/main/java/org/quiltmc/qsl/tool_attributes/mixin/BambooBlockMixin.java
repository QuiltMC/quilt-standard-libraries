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

package org.quiltmc.qsl.tool_attributes.mixin;

import net.minecraft.block.BambooBlock;
import net.minecraft.block.BambooSaplingBlock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.BlockView;
import org.quiltmc.qsl.tool_attributes.api.QuiltToolTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({BambooBlock.class, BambooSaplingBlock.class})
public abstract class BambooBlockMixin {
	/**
	 * When the player is holding a {@link net.minecraft.item.SwordItem SwordItem}, Bamboo return {@code 1.0F} and is instantly mined.
	 *
	 * <p>This injection provides that same functionality when mining with items that are in the {@link QuiltToolTags#SWORDS quilt:swords} tag</p>
	 */
	@Inject(at = @At("HEAD"), method = "calcBlockBreakingDelta", cancellable = true)
	private void onCalcBlockBreakingDelta(BlockState state, PlayerEntity player, BlockView world, BlockPos pos, CallbackInfoReturnable<Float> info) {
		if (QuiltToolTags.SWORDS.contains(player.getMainHandStack().getItem())) {
			info.setReturnValue(1.0f);
		}
	}
}
