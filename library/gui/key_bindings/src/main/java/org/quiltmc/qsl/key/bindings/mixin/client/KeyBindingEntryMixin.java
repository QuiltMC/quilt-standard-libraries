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

import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.option.KeyBindingListWidget;
import net.minecraft.client.gui.widget.option.KeyBindingListWidget.KeyBindingEntry;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Formatting;

import org.quiltmc.qsl.key.bindings.api.KeyBindingRegistry;
import org.quiltmc.qsl.key.bindings.impl.ConflictTooltipOwner;
import org.quiltmc.qsl.key.bindings.impl.KeyBindingRegistryImpl;

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
	private List<Text> quilt$conflictTooltips = new ArrayList<>(2);

	@Unique
	private InputUtil.Key quilt$previousBoundKey;

	@Unique
	private static InputUtil.Key quilt$changedBoundKey;

	@Shadow(aliases = "field_2742", remap = false)
	@Final
	KeyBindingListWidget field_2742;

	@Inject(method = "<init>", at = @At("TAIL"))
	private void initPreviousBoundKey(KeyBindingListWidget list, KeyBinding keyBinding, Text text, CallbackInfo ci) {
		this.quilt$previousBoundKey = null;
		quilt$changedBoundKey = null;
	}

	@Inject(
			method = "render",
			at = @At(
				value = "INVOKE",
				target = "Lnet/minecraft/client/option/KeyBinding;isUnbound()Z"
			),
			locals = LocalCapture.CAPTURE_FAILHARD
	)
	private void collectConflictTooltips(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta, CallbackInfo ci, boolean bl, boolean bl2) {
		InputUtil.Key boundKey = KeyBindingRegistry.getBoundKey(this.binding);

		if (!boundKey.equals(this.quilt$previousBoundKey) || quilt$changedBoundKey != null) {
			this.quilt$conflictTooltips.clear();

			if (quilt$changedBoundKey != null && quilt$changedBoundKey.equals(boundKey)) {
				quilt$changedBoundKey = null;
			} else {
				quilt$changedBoundKey = boundKey;
			}

			if (!this.binding.isUnbound()) {
				for (KeyBinding keyBinding : KeyBindingRegistryImpl.getKeyBindings()) {
					if (keyBinding != this.binding && this.binding.equals(keyBinding)) {
						if (this.quilt$conflictTooltips.isEmpty()) {
							this.quilt$conflictTooltips.add(new TranslatableText("key.qsl.key_conflict.tooltip").formatted(Formatting.RED));
						}

						this.quilt$conflictTooltips.add(new TranslatableText("key.qsl.key_conflict.tooltip.entry", new TranslatableText(keyBinding.getTranslationKey())).formatted(Formatting.RED));
					}
				}
			}
		}

		this.quilt$previousBoundKey = boundKey;
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

	@Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
	private void addMiddleButtonBehavior(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
		if (button == GLFW.GLFW_MOUSE_BUTTON_MIDDLE) {
			if (this.bindButton.active && this.bindButton.visible) {
				if (((ClickableWidgetAccessor) this.bindButton).callClicked(mouseX, mouseY)) {
					MinecraftClient client = ((EntryListWidgetAccessor) (Object) field_2742).getClient();
					this.bindButton.playDownSound(client.getSoundManager());
					this.binding.setBoundKey(InputUtil.UNKNOWN_KEY);
					KeyBinding.updateKeysByCode();

					cir.setReturnValue(true);
				}
			}
		}
	}
}
