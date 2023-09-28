/*
 * Copyright 2023 The Quilt Project
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

package org.quiltmc.qsl.registry.mixin.compat;

import com.mojang.datafixers.DataFixerBuilder;
import com.mojang.datafixers.schemas.Schema;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import net.minecraft.datafixer.Schemas;

import org.quiltmc.qsl.registry.impl.datafixer.fix.QuiltStatusEffectIdFix;

@Mixin(Schemas.class)
public class SchemasMixin {
	@Inject(
			method = "build",
			at = @At(
				value = "INVOKE",
				target = "Lcom/mojang/datafixers/DataFixerBuilder;addFixer(Lcom/mojang/datafixers/DataFix;)V"
			),
			slice = @Slice(
				from = @At(value = "CONSTANT", args = "intValue=3568")
			),
			locals = LocalCapture.CAPTURE_FAILEXCEPTION
	)
	private static void injectQuiltStatusEffectIdFix(DataFixerBuilder builder, CallbackInfo ci, Schema schema192) {
		builder.addFixer(new QuiltStatusEffectIdFix(schema192));
	}
}
