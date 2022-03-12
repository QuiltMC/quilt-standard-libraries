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

package org.quiltmc.qsl.key.binds.mixin.client.config;

import com.mojang.blaze3d.platform.InputUtil;

import org.quiltmc.qsl.key.binds.impl.config.QuiltKeyBindsConfigManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBind;

@Mixin(GameOptions.class)
public abstract class GameOptionsMixin {
    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/option/GameOptions;write()V"
        ),
        method = "setKeyCode"
    )
    private void writeToKeyBindConfig(KeyBind key, InputUtil.Key code, CallbackInfo ci) {
        QuiltKeyBindsConfigManager.populateConfig();
        QuiltKeyBindsConfigManager.saveModConfig();
    }

    @Redirect(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/option/KeyBind;setBoundKey(Lcom/mojang/blaze3d/platform/InputUtil$Key;)V"
        ),
        method = "accept"
    )
    private void useOurConfigInstead(KeyBind keyBind, InputUtil.Key key) {}
}
