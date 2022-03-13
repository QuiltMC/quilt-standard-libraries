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

package org.quiltmc.qsl.key.binds.mixin.client.chords;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.mojang.blaze3d.platform.InputUtil;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.qsl.key.binds.impl.chords.ChordedKeyBind;
import org.quiltmc.qsl.key.binds.impl.chords.KeyChord;
import org.quiltmc.qsl.key.binds.impl.config.QuiltKeyBindsConfigManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.KeyBindsScreen;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBind;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

// TODO - Support mouse buttons on chords
@Mixin(KeyBindsScreen.class)
public abstract class KeyBindsScreenMixin extends GameOptionsScreen {
    @Shadow
    @Nullable
    public KeyBind focusedKey;

    @Shadow
    public long time;
    
    @Unique
    private List<InputUtil.Key> quilt$protoChord;

    public KeyBindsScreenMixin(Screen screen, GameOptions gameOptions, Text text) {
        super(screen, gameOptions, text);
    }

    @Inject(at = @At("TAIL"), method = "init")
    private void initializeProtoChord(CallbackInfo ci) {
        this.quilt$protoChord = new ArrayList<>();
    }

    @Inject(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/option/GameOptions;setKeyCode(Lnet/minecraft/client/option/KeyBind;Lcom/mojang/blaze3d/platform/InputUtil$Key;)V",
            ordinal = 1
        ),
        method = "keyPressed",
        cancellable = true
    )
    private void modifyKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        InputUtil.Key key = InputUtil.fromKeyCode(keyCode, scanCode);
        if (!quilt$protoChord.contains(key)) {
            quilt$protoChord.add(key);
        }
        cir.setReturnValue(true);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (this.focusedKey != null) {
            if (quilt$protoChord.size() == 1) {
                this.gameOptions.setKeyCode(this.focusedKey, quilt$protoChord.get(0));
            } else if (quilt$protoChord.size() > 1) {
                SortedMap<InputUtil.Key, Boolean> map = new TreeMap<>();
                for (int i = 0; i < quilt$protoChord.size(); i++) {
                    map.put(quilt$protoChord.get(i), false);
                }
                ((ChordedKeyBind)this.focusedKey).setBoundChord(new KeyChord(map));
                QuiltKeyBindsConfigManager.populateConfig();
                QuiltKeyBindsConfigManager.saveModConfig();
            }

            quilt$protoChord.clear();
            this.focusedKey = null;
			this.time = Util.getMeasuringTimeMs();
			KeyBind.updateBoundKeys();
			return true;
        } else {
            return super.keyReleased(keyCode, scanCode, modifiers);
        }
    }
}