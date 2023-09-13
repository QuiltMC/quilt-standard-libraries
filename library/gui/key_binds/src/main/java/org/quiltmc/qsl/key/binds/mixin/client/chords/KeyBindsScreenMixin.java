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

import java.util.List;
import java.util.SortedMap;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import it.unimi.dsi.fastutil.objects.Object2BooleanAVLTreeMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

import com.mojang.blaze3d.platform.InputUtil;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.KeyBindsScreen;
import net.minecraft.client.gui.widget.option.KeyBindListWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.KeyBind;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import org.quiltmc.qsl.key.binds.impl.chords.KeyChord;
import org.quiltmc.qsl.key.binds.impl.config.QuiltKeyBindsConfigManager;

@Mixin(KeyBindsScreen.class)
public abstract class KeyBindsScreenMixin extends GameOptionsScreen {
	@Shadow
	@Nullable
	public KeyBind focusedKey;

	@Shadow
	public long time;
	@Shadow
	private KeyBindListWidget keyBindList;

	@Unique
	private List<InputUtil.Key> quilt$focusedProtoChord;

	@Unique
	private boolean quilt$initialMouseRelease;

	public KeyBindsScreenMixin(Screen screen, GameOptions gameOptions, Text text) {
		super(screen, gameOptions, text);
	}

	@Inject(at = @At("TAIL"), method = "init")
	private void initializeProtoChord(CallbackInfo ci) {
		this.quilt$focusedProtoChord = new ObjectArrayList<>();
		this.quilt$initialMouseRelease = true;
	}

	@Inject(
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/option/GameOptions;setKeyCode(Lnet/minecraft/client/option/KeyBind;Lcom/mojang/blaze3d/platform/InputUtil$Key;)V"
			),
			method = "mouseClicked",
			cancellable = true
	)
	private void modifyMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		InputUtil.Key key = InputUtil.Type.MOUSE.createFromKeyCode(button);
		if (!this.quilt$focusedProtoChord.contains(key)) {
			this.quilt$focusedProtoChord.add(key);
		}

		cir.setReturnValue(true);
	}

	@Inject(at = @At(value = "RETURN", ordinal = 1), method = "mouseClicked")
	private void excludeFirstMouseClick(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		this.quilt$initialMouseRelease = true;
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
		var key = InputUtil.fromKeyCode(keyCode, scanCode);
		if (!this.quilt$focusedProtoChord.contains(key)) {
			this.quilt$focusedProtoChord.add(key);
		}

		cir.setReturnValue(true);
	}

	@Override
	public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
		if (this.focusedKey != null) {
			if (this.quilt$focusedProtoChord.size() == 1) {
				this.gameOptions.setKeyCode(this.focusedKey, this.quilt$focusedProtoChord.get(0));
			} else if (this.quilt$focusedProtoChord.size() > 1) {
				SortedMap<InputUtil.Key, Boolean> map = new Object2BooleanAVLTreeMap<>();
				for (InputUtil.Key key : this.quilt$focusedProtoChord) {
					map.put(key, false);
				}

				this.focusedKey.setBoundChord(new KeyChord(map));
				QuiltKeyBindsConfigManager.updateConfig(false);
			}

			this.quilt$focusedProtoChord.clear();
			this.focusedKey = null;
			this.time = Util.getMeasuringTimeMs();
			this.keyBindList.update();

			return true;
		} else {
			return super.keyReleased(keyCode, scanCode, modifiers);
		}
	}

	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		// TODO - Don't duplicate code, have a common method
		if (this.focusedKey != null && !this.quilt$initialMouseRelease) {
			if (this.quilt$focusedProtoChord.size() == 1) {
				this.gameOptions.setKeyCode(this.focusedKey, this.quilt$focusedProtoChord.get(0));
			} else if (this.quilt$focusedProtoChord.size() > 1) {
				SortedMap<InputUtil.Key, Boolean> map = new Object2BooleanAVLTreeMap<>();
				for (InputUtil.Key key : this.quilt$focusedProtoChord) {
					map.put(key, false);
				}

				this.focusedKey.setBoundChord(new KeyChord(map));
				QuiltKeyBindsConfigManager.updateConfig(false);
			}

			this.quilt$focusedProtoChord.clear();
			this.focusedKey = null;
			this.time = Util.getMeasuringTimeMs();
			this.keyBindList.update();

			return true;
		} else {
			this.quilt$initialMouseRelease = false;
			return super.mouseReleased(mouseX, mouseY, button);
		}
	}
}
