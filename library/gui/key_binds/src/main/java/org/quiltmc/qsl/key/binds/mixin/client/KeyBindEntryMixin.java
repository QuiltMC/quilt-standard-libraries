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

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.platform.InputUtil;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.option.KeyBindListWidget;
import net.minecraft.client.gui.widget.option.KeyBindListWidget.KeyBindEntry;
import net.minecraft.client.option.KeyBind;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import org.quiltmc.qsl.key.binds.api.KeyBindRegistry;
import org.quiltmc.qsl.key.binds.impl.ConflictTooltipOwner;
import org.quiltmc.qsl.key.binds.impl.KeyBindRegistryImpl;

@Environment(EnvType.CLIENT)
@Mixin(KeyBindEntry.class)
public abstract class KeyBindEntryMixin extends KeyBindListWidget.Entry implements ConflictTooltipOwner {
	@Shadow
	@Final
	private KeyBind key;

	@Shadow
	@Final
	private ButtonWidget bindButton;

	@Unique
	private List<Text> quilt$conflictTooltips = new ArrayList<>(2);

	@Unique
	private static InputUtil.Key quilt$previousBoundKey;

	@Unique
	private static InputUtil.Key quilt$changedBoundKey;

	@Shadow(aliases = "field_2742", remap = false)
	@Final
	KeyBindListWidget field_2742;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void initPreviousBoundKey(KeyBindListWidget list, KeyBind key, Text text, CallbackInfo ci) {
		quilt$previousBoundKey = null;
		quilt$changedBoundKey = null;
	}

	@Inject(
			method = "render",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/option/KeyBind;isUnbound()Z"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void collectConflictTooltips(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci, boolean bl, boolean bl2) {
		InputUtil.Key boundKey = KeyBindRegistry.getBoundKey(this.key);

		if (!boundKey.equals(quilt$previousBoundKey) || quilt$changedBoundKey != null) {
			this.quilt$conflictTooltips.clear();

			if (quilt$changedBoundKey != null && quilt$changedBoundKey.equals(boundKey)) {
				quilt$changedBoundKey = null;
			} else {
				quilt$changedBoundKey = boundKey;
			}

			if (!this.key.isUnbound()) {
				for (KeyBind otherKey : KeyBindRegistryImpl.getKeyBinds()) {
					if (otherKey != this.key && this.key.keyEquals(otherKey)) {
						if (this.quilt$conflictTooltips.isEmpty()) {
							this.quilt$conflictTooltips.add(new TranslatableText("key.qsl.key_conflict.tooltip").formatted(Formatting.RED));
						}

						this.quilt$conflictTooltips.add(new TranslatableText("key.qsl.key_conflict.tooltip.entry", new TranslatableText(otherKey.getTranslationKey())).formatted(Formatting.RED));
					}
				}
			}
		}

		quilt$previousBoundKey = boundKey;
	}

	@ModifyArg(
			method = "render",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/gui/widget/ButtonWidget;setMessage(Lnet/minecraft/text/Text;)V",
				ordinal = 2
			)
	)
	private Text addConflictIndicator(Text originalText) {
		return new TranslatableText("key.qsl.key_conflict.indicator", originalText).formatted(Formatting.RED);
	}

	@Override
	public List<Text> getConflictTooltips() {
		return this.quilt$conflictTooltips;
	}
}
