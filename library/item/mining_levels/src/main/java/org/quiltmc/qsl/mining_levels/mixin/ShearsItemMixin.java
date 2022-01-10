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
import net.minecraft.item.ShearsItem;
import org.quiltmc.qsl.mining_levels.api.QuiltMineableTags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Adds support for {@link org.quiltmc.qsl.mining_levels.api.QuiltMineableTags#SHEARS_MINEABLE}.
 */
@Mixin(ShearsItem.class)
abstract class ShearsItemMixin {
	@Inject(method = "isSuitableFor", at = @At("HEAD"), cancellable = true)
	private void quilt$onIsSuitableFor(BlockState state, CallbackInfoReturnable<Boolean> info) {
		if (state.isIn(QuiltMineableTags.SHEARS_MINEABLE)) {
			info.setReturnValue(true);
		}
	}
}
