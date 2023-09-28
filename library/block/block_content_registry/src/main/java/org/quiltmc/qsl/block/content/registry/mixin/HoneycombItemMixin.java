/*
 * Copyright 2022-2023 QuiltMC
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
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.item.HoneycombItem;

import org.quiltmc.qsl.block.content.registry.impl.BlockContentRegistriesImpl;

@Mixin(HoneycombItem.class)
public class HoneycombItemMixin {
	// Lambda in assignment of UNWAXED_TO_WAXED_BLOCKS
	// Replaces old map with one updated by our API
	@Inject(
			slice = @Slice(
					from = @At(
							value = "INVOKE",
							target = "Lcom/google/common/collect/ImmutableBiMap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableBiMap$Builder;"
					)
			),
			method = "method_34723()Lcom/google/common/collect/BiMap;",
			at = @At("RETURN"),
			cancellable = true
	)
	private static void quilt$createOxidationLevelIncreasesMap(CallbackInfoReturnable<BiMap<Block, Block>> cir) {
		BlockContentRegistriesImpl.INITIAL_WAXED_BLOCKS.putAll(cir.getReturnValue());
		cir.setReturnValue(BlockContentRegistriesImpl.UNWAXED_WAXED_BLOCKS);
	}

	// Lambda in assignment of WAXED_TO_UNWAXED_BLOCKS
	// Replaces old map with one updated by our API
	@Inject(
			slice = @Slice(
					from = @At(value = "FIELD", target = "Lnet/minecraft/item/HoneycombItem;UNWAXED_TO_WAXED_BLOCKS:Ljava/util/function/Supplier;")
			),
			method = "method_34722()Lcom/google/common/collect/BiMap;",
			at = @At("RETURN"),
			cancellable = true
	)
	private static void quilt$createOxidationLevelDecreasesMap(CallbackInfoReturnable<BiMap<Block, Block>> cir) {
		cir.setReturnValue(BlockContentRegistriesImpl.WAXED_UNWAXED_BLOCKS);
	}
}
