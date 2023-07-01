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

package org.quiltmc.qsl.registry.mixin;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.util.math.MathHelper;

import org.quiltmc.qsl.registry.impl.dynamic.DynamicMetaRegistryImpl;

@Mixin(Registries.class)
public class RegistriesMixin {
	@Inject(method = "freeze", at = @At("RETURN"))
	private static void onFreezeBuiltins(CallbackInfo ci) {
		//region Fix MC-197259
		final List<BlockState> states = Registries.BLOCK.stream()
				.flatMap(block -> block.getStateManager().getStates().stream())
				.toList();

		final int xLength = MathHelper.ceil(MathHelper.sqrt(states.size()));
		final int zLength = MathHelper.ceil(states.size() / (float) xLength);

		DebugChunkGeneratorAccessor.setBlockStates(states);
		DebugChunkGeneratorAccessor.setXSideLength(xLength);
		DebugChunkGeneratorAccessor.setZSideLength(zLength);
		//endregion

		DynamicMetaRegistryImpl.freeze();
	}
}
