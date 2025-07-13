/*
 * Copyright 2021 The Quilt Project
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

package org.quiltmc.qsl.registry.attachment.test.mixin.client;

import java.util.ArrayList;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.hud.debug.DebugHud;
import net.minecraft.util.Formatting;

import org.quiltmc.qsl.registry.attachment.test.client.ClientAttachmentTest;

@Mixin(DebugHud.class)
abstract class DebugHudMixin {
	@ModifyExpressionValue(
			method = "getRightText", require = 1, allow = 1,
			at = @At(
				value = "INVOKE", remap = false,
				target = "Lcom/google/common/collect/Lists;newArrayList([Ljava/lang/Object;)Ljava/util/ArrayList;"
			)
	)
	private ArrayList<String> shareLines(ArrayList<String> list, @Share("lines") LocalRef<List<String>> lines) {
		lines.set(list);
		return list;
	}

	@ModifyExpressionValue(
			method = "getRightText", require = 1, allow = 1,
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/world/ClientWorld;getBlockState(Lnet/minecraft/util/math/BlockPos;)"
					+ "Lnet/minecraft/block/BlockState;"
			)
	)
	private BlockState shareState(BlockState blockState, @Share("state") LocalRef<BlockState> state) {
		state.set(blockState);
		return blockState;
	}

	// puts BASED after block state
	@Inject(
			method = "getRightText",
			slice = @Slice(from = @At(
				value = "FIELD",
				target = "Lnet/minecraft/registry/Registries;BLOCK:Lnet/minecraft/registry/DefaultedRegistry;"
			)),
			at = @At(
				value = "INVOKE", ordinal = 0, shift = At.Shift.AFTER,
				target = "Ljava/util/List;add(Ljava/lang/Object;)Z"
			)
	)
	public void quilt$addTestAttachment(
			CallbackInfoReturnable<List<String>> cir,
			@Share("lines") LocalRef<List<String>> lines,
			@Share("state") LocalRef<BlockState> state
	) {
		Boolean value = ClientAttachmentTest.BASED.getNullable(state.get().getBlock());
		String valueStr;
		if (value == null) {
			valueStr = Formatting.BLUE + "unset";
		} else if (value) {
			valueStr = Formatting.GREEN + "yes";
		} else {
			valueStr = Formatting.RED + "no";
		}

		lines.get().add("[Quilt] based: " + valueStr);
	}
}
