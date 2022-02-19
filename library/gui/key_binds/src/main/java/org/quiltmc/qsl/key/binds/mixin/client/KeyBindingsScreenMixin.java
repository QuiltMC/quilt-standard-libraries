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

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.option.GameOptionsScreen;
import net.minecraft.client.gui.screen.option.KeyBindsScreen;
import net.minecraft.client.gui.widget.option.KeyBindListWidget;
import net.minecraft.client.gui.widget.option.KeyBindListWidget.KeyBindEntry;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import org.quiltmc.qsl.key.binds.impl.ConflictTooltipOwner;

@Environment(EnvType.CLIENT)
@Mixin(KeyBindsScreen.class)
public abstract class KeyBindingsScreenMixin extends GameOptionsScreen {
	@Shadow
	private KeyBindListWidget keybindsList;

	private KeyBindingsScreenMixin(Screen screen, GameOptions gameOptions, Text text) {
		super(screen, gameOptions, text);
	}

	@SuppressWarnings("unchecked")
	@Inject(method = "render", at = @At("TAIL"))
	private void renderConflictTooltips(MatrixStack matrices, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		// TODO - Somehow extend the hover area to include the label too
		KeyBindListWidget.Entry entry = ((EntryListWidgetAccessor<KeyBindListWidget.Entry>) this.keybindsList).invokeGetHoveredEntry();
		if (entry != null && entry instanceof KeyBindEntry keyBindEntry) {
			List<Text> tooltipLines = ((ConflictTooltipOwner) keyBindEntry).getConflictTooltips();
			this.renderTooltip(matrices, tooltipLines, mouseX, mouseY);
		}
	}
}
