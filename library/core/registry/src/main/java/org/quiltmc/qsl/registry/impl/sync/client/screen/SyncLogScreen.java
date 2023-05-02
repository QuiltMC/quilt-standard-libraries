/*
 * Copyright 2023 QuiltMC
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

package org.quiltmc.qsl.registry.impl.sync.client.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.ScreenTexts;
import net.minecraft.text.Text;
import org.quiltmc.qsl.registry.impl.sync.client.LogBuilder;

import java.util.List;

public class SyncLogScreen extends Screen {
	private final Screen parent;
	private final List<LogBuilder.Section> text;
	private ScrollableMultiTextWidget scrollableText;
	private double currentScroll = 0;

	public SyncLogScreen(Screen parent, List<LogBuilder.Section> text) {
		super(Text.translatable("quilt.core.registry_sync.logs_title", "Server Synchronization Logs"));
		this.parent = parent;
		this.text = text;
	}

	@Override
	protected void init() {
		super.init();
		this.scrollableText = new ScrollableMultiTextWidget(this.client, 40, 40, this.width - 80, this.height - 90, this.text, this.currentScroll, (s) -> this.currentScroll = s);
		this.addDrawableChild(this.scrollableText);

		int y = this.height - 40;

		{
			int x = this.width / 2 - 5 - 120;
			this.addDrawableChild(ButtonWidget.builder(Text.translatable("chat.copy"), (button) -> {
				this.client.keyboard.setClipboard(LogBuilder.stringify(this.text));
			}).positionAndSize(x, y, 120, 20).build());
		}

		{
			int x = this.width / 2 + 5;
			this.addDrawableChild(ButtonWidget.builder(ScreenTexts.BACK, (button) -> {
				this.client.setScreen(this.parent);
			}).positionAndSize(x, y, 120, 20).build());
		}
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		this.renderBackground(matrices);
		drawCenteredText(matrices, this.textRenderer, this.getTitle(), this.width / 2, 20, 0xFFFFFF);
		super.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public void closeScreen() {
		this.client.setScreen(parent);
	}
}
