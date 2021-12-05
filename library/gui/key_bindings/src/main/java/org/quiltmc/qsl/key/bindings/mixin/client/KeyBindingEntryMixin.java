/*
 * Copyright 2021 QuiltMC
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

package org.quiltmc.qsl.key.bindings.mixin.client;

import java.util.ArrayList;
import java.util.List;

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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.option.KeyBindingListWidget;
import net.minecraft.client.gui.widget.option.KeyBindingListWidget.KeyBindingEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import org.quiltmc.qsl.key.bindings.impl.ConflictTooltipOwner;

@Environment(EnvType.CLIENT)
@Mixin(KeyBindingEntry.class)
public abstract class KeyBindingEntryMixin extends KeyBindingListWidget.Entry implements ConflictTooltipOwner {
	@Shadow
	@Final
	private KeyBinding binding;

	@Shadow
	@Final
	private ButtonWidget bindButton;

	@Unique
	private List<Text> quilt$conflictTooltips = new ArrayList<>(1);

	@Shadow(aliases = "field_2742")
	@Final
	KeyBindingListWidget field_2742;

	// FIXME - Find a way to get all conflicting keys that reuses the iteration done by Vanilla
	@Inject(
			method = "render",
			at = @At(
				value = "INVOKE_ASSIGN",
				target = "Lnet/minecraft/client/option/KeyBinding;isUnbound()Z",
				shift = At.Shift.AFTER
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void collectConflictTooltips(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci, boolean bl, boolean bl2) {
		quilt$conflictTooltips.clear();
		MinecraftClient client = ((EntryListWidgetAccessor) (Object) field_2742).getClient();
		for (KeyBinding keyBinding : client.options.keysAll) {
			if (keyBinding != this.binding && this.binding.equals(keyBinding)) {
				quilt$conflictTooltips.add(new TranslatableText("key.qsl.key_conflict.tooltip.entry", new TranslatableText(keyBinding.getTranslationKey())).formatted(Formatting.RED));
			}
		}
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
		List<Text> returnedText = this.quilt$conflictTooltips;
		returnedText.add(0, new TranslatableText("key.qsl.key_conflict.tooltip").formatted(Formatting.RED));
		return returnedText;
	}
}
