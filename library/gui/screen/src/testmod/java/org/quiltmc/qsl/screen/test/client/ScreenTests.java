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

package org.quiltmc.qsl.screen.test.client;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import org.quiltmc.qsl.screen.api.client.QuiltScreen;
import org.quiltmc.qsl.screen.api.client.ScreenEvents;

public class ScreenTests implements ScreenEvents.AfterInit, ScreenEvents.AfterRender {
	public static final Logger LOGGER = LoggerFactory.getLogger("ScreenEventsTest");
	private Screen actualScreen;

	@Override
	public void afterInit(Screen screen, MinecraftClient client, int scaledWidth, int scaledHeight) {
		if (screen instanceof TitleScreen) {
			final List<ClickableWidget> buttons = ((QuiltScreen) screen).getButtons();

			buttons.add(
					new ButtonWidget((screen.width / 2) + 120, ((screen.height / 4) + 95), 70, 20, Text.of("Hello world!!"),
							button -> {
								LOGGER.info("Hello world!!");
							})
			);

			this.actualScreen = screen;
		} else {
			this.actualScreen = null;
		}
	}

	@Override
	public void afterRender(Screen screen, MatrixStack matrices, int mouseX, int mouseY, float tickDelta) {
		if (screen == this.actualScreen) {
			RenderSystem.setShaderTexture(0, InGameHud.GUI_ICONS_TEXTURE);
			DrawableHelper.drawTexture(matrices, (screen.width / 2) - 124, (screen.height / 4) + 96, 20, 20, 34, 9, 9, 9, 256, 256);
		}
	}
}
