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

package org.quiltmc.qsl.block.content.registry.mixin;

import com.google.common.collect.BiMap;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.item.HoneycombItem;

import org.quiltmc.qsl.block.content.registry.impl.BlockContentRegistriesInitializer;

@Mixin(HoneycombItem.class)
public class HoneycombItemMixin {
	@Dynamic("Replace old map with one updated by Registry Attachments")
	@Inject(method = "method_34723", at = @At("RETURN"), cancellable = true, remap = false)
	private static void createOxidationLevelIncreasesMap(CallbackInfoReturnable<BiMap<Block, Block>> cir) {
		BlockContentRegistriesInitializer.INITIAL_WAXED_BLOCKS.putAll(cir.getReturnValue());
		cir.setReturnValue(BlockContentRegistriesInitializer.UNWAXED_WAXED_BLOCKS);
	}

	@Dynamic("Replace old map with one updated by Registry Attachments")
	@Inject(method = "method_34722", at = @At("RETURN"), cancellable = true, remap = false)
	private static void createOxidationLevelDecreasesMap(CallbackInfoReturnable<BiMap<Block, Block>> cir) {
		cir.setReturnValue(BlockContentRegistriesInitializer.WAXED_UNWAXED_BLOCKS);
	}
}
