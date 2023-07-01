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

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.block.BlockState;
import net.minecraft.client.gui.hud.DebugHud;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;

import org.quiltmc.qsl.registry.attachment.test.client.ClientAttachmentTest;

@Mixin(DebugHud.class)
public abstract class DebugHudMixin {
	@Inject(method = "getRightText", at = @At(value = "INVOKE", target = "Ljava/util/List;add(Ljava/lang/Object;)Z", ordinal = 2,
			shift = At.Shift.AFTER), locals = LocalCapture.CAPTURE_FAILHARD)
	public void quilt$addTestAttachment(CallbackInfoReturnable<List<String>> cir, long l, long m, long n, long o,
			List<String> list, BlockPos blockPos, BlockState blockState) {
		Boolean value = ClientAttachmentTest.BASED.getNullable(blockState.getBlock());
		String valueStr;
		if (value == null) {
			valueStr = Formatting.BLUE + "unset";
		} else if (value) {
			valueStr = Formatting.GREEN + "yes";
		} else {
			valueStr = Formatting.RED + "no";
		}

		list.add("[Quilt] based: " + valueStr);
	}
}
