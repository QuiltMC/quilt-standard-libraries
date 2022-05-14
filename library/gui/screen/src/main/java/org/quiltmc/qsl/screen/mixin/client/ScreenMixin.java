/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
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

package org.quiltmc.qsl.screen.mixin.client;

import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.item.ItemRenderer;

import org.quiltmc.qsl.screen.api.client.QuiltScreenExtensions;
import org.quiltmc.qsl.screen.api.client.ScreenEvents;
import org.quiltmc.qsl.screen.impl.client.ButtonList;

@Mixin(Screen.class)
abstract class ScreenMixin implements QuiltScreenExtensions {
	@Shadow
	@Final
	private List<Selectable> selectables;
	@Shadow
	@Final
	private List<Element> children;
	@Shadow
	@Final
	private List<Drawable> drawables;

	@Shadow
	protected MinecraftClient client;

	@Shadow
	protected ItemRenderer itemRenderer;

	@Shadow
	protected TextRenderer textRenderer;

	@Unique
	private ButtonList quilt$quiltButtons;

	@Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At("HEAD"))
	private void beforeInitScreen(MinecraftClient client, int width, int height, CallbackInfo ci) {
		this.quilt$quiltButtons = null;

		ScreenEvents.BEFORE_INIT.invoker().beforeInit((Screen) (Object) this, client, width, height);
	}

	@Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At("TAIL"))
	private void afterInitScreen(MinecraftClient client, int width, int height, CallbackInfo ci) {
		ScreenEvents.AFTER_INIT.invoker().afterInit((Screen) (Object) this, client, width, height);
	}

	@Override
	public List<ClickableWidget> getButtons() {
		// Lazy init to make the list access safe after Screen#init
		if (this.quilt$quiltButtons == null) {
			this.quilt$quiltButtons = new ButtonList(this.drawables, this.selectables, this.children);
		}

		return this.quilt$quiltButtons;
	}

	@Override
	public ItemRenderer getItemRenderer() {
		return this.itemRenderer;
	}

	@Override
	public TextRenderer getTextRenderer() {
		return this.textRenderer;
	}

	@Override
	public MinecraftClient getClient() {
		return this.client;
	}
}
