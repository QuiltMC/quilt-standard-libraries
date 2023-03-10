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
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.block.Block;
import net.minecraft.block.Oxidizable;

import org.quiltmc.qsl.block.content.registry.impl.BlockContentRegistriesInitializer;

@Mixin(Oxidizable.class)
public interface OxidizableMixin {
	// lambda in assignment of OXIDATION_LEVEL_INCREASES
	// replaces old map with one updated by registry attachments
	@Inject(
			slice = @Slice(from = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableBiMap$Builder;put(Ljava/lang/Object;Ljava/lang/Object;)Lcom/google/common/collect/ImmutableBiMap$Builder;")),
			method = "method_34740()Lcom/google/common/collect/BiMap;",
			at = @At("RETURN"),
			cancellable = true
	)
	private static void createOxidationLevelIncreasesMap(CallbackInfoReturnable<BiMap<Block, Block>> cir) {
		BlockContentRegistriesInitializer.INITIAL_OXIDATION_BLOCKS.putAll(cir.getReturnValue());
		cir.setReturnValue(BlockContentRegistriesInitializer.OXIDATION_INCREASE_BLOCKS);
	}


	// Lambda in assignment of OXIDATION_LEVEL_DECREASES
	// Replace old map with one updated by our api
	@Inject(
			slice = @Slice(from = @At(value = "FIELD", target = "Lnet/minecraft/block/Oxidizable;OXIDATION_LEVEL_INCREASES:Ljava/util/function/Supplier;")),
			method = "method_34739()Lcom/google/common/collect/BiMap;",
			at = @At("RETURN"),
			cancellable = true
	)

	private static void createOxidationLevelDecreasesMap(CallbackInfoReturnable<BiMap<Block, Block>> cir) {
		cir.setReturnValue(BlockContentRegistriesInitializer.OXIDATION_DECREASE_BLOCKS);
	}
}
