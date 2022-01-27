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

package org.quiltmc.qsl.mining_levels.mixin;

import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SwordItem;
import net.minecraft.item.ToolItem;
import net.minecraft.item.ToolMaterial;
import org.quiltmc.qsl.mining_levels.api.MiningLevelManager;
import org.quiltmc.qsl.mining_levels.api.QuiltMineableTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds support for {@link QuiltMineableTags#SWORD_MINEABLE}.
 */
@Mixin(SwordItem.class)
abstract class SwordItemMixin extends ToolItem {
	private SwordItemMixin(ToolMaterial material, Settings settings) {
		super(material, settings);
	}

	@Inject(method = "isSuitableFor", at = @At("HEAD"), cancellable = true)
	private void quilt$onIsSuitableFor(BlockState state, CallbackInfoReturnable<Boolean> info) {
		if (state.isIn(QuiltMineableTags.SWORD_MINEABLE)) {
			int miningLevel = getMaterial().getMiningLevel();

			if (miningLevel >= MiningLevelManager.getRequiredMiningLevel(state)) {
				info.setReturnValue(true);
			}
		}
	}

	@Inject(method = "getMiningSpeedMultiplier", at = @At("RETURN"), cancellable = true)
	private void quilt$onGetMiningSpeedMultiplier(ItemStack stack, BlockState state, CallbackInfoReturnable<Float> info) {
		if (info.getReturnValueF() == 1f) { // If not caught by vanilla checks
			if (state.isIn(QuiltMineableTags.SWORD_MINEABLE)) { // mimics MiningToolItem.getMiningSpeedMultiplier
				info.setReturnValue(getMaterial().getMiningSpeedMultiplier());
			}
		}
	}
}
