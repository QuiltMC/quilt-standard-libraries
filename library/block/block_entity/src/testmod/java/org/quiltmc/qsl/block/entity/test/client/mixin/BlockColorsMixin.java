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

package org.quiltmc.qsl.block.entity.test.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.color.block.BlockColors;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.block.entity.test.BlockEntityTypeTest;
import org.quiltmc.qsl.block.entity.test.client.BlockEntityTypeTestClient;

@ClientOnly
@Mixin(BlockColors.class)
public class BlockColorsMixin {
	@Inject(method = "create", at = @At("RETURN"))
	private static void onCreate(CallbackInfoReturnable<BlockColors> cir) {
		// TODO: use QSL color provider API instead once available
		cir.getReturnValue().registerColorProvider(BlockEntityTypeTestClient.ANGY_BLOCK_COLOR_PROVIDER,
				BlockEntityTypeTest.INITIAL_ANGY_BLOCK,
				BlockEntityTypeTest.BUILDER_ADDED_ANGY_BLOCK,
				BlockEntityTypeTest.BUILDER_MULTI_1_ANGY_BLOCK,
				BlockEntityTypeTest.BUILDER_MULTI_2_ANGY_BLOCK,
				BlockEntityTypeTest.POST_ADDED_ANGY_BLOCK,
				BlockEntityTypeTest.POST_MULTI_1_ANGY_BLOCK,
				BlockEntityTypeTest.POST_MULTI_2_ANGY_BLOCK
		);
	}
}
