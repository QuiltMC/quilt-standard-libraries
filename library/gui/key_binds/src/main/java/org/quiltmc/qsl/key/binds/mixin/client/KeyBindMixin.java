/*
 * Copyright 2021-2022 QuiltMC
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

package org.quiltmc.qsl.key.binds.mixin.client;

import java.util.Map;

import com.mojang.blaze3d.platform.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.option.KeyBind;

@Environment(EnvType.CLIENT)
@Mixin(KeyBind.class)
public abstract class KeyBindMixin {
	@Shadow
	@Mutable
	@Final
	private static Map<String, Integer> ORDER_BY_CATEGORIES;

	@Inject(method = "<init>(Ljava/lang/String;Lcom/mojang/blaze3d/platform/InputUtil$Type;ILjava/lang/String;)V", at = @At("TAIL"))
	private void addModdedCategory(String translationKey, InputUtil.Type type, int keyCode, String category, CallbackInfo ci) {
		if (!ORDER_BY_CATEGORIES.containsKey(category)) {
			ORDER_BY_CATEGORIES.put(category, ORDER_BY_CATEGORIES.size() + 1);
		}
	}
}
