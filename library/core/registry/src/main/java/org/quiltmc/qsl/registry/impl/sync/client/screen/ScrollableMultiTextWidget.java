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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.GridWidget;
import net.minecraft.client.gui.widget.MultilineTextWidget;
import net.minecraft.client.gui.widget.ScrollableWidget;
import net.minecraft.client.gui.widget.SpacerWidget;
import net.minecraft.client.gui.widget.container.LayoutSettings;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import org.quiltmc.qsl.registry.impl.sync.client.LogBuilder;

import java.util.List;
import java.util.function.DoubleConsumer;

public class ScrollableMultiTextWidget extends ScrollableWidget {
	private final GridWidget.AdditionHelper helper;
	private final LayoutSettings headerSettings;
	private final DoubleConsumer scrollUpdater;
	private GridWidget grid;
	private MutableText narration = Text.empty();

	public ScrollableMultiTextWidget(MinecraftClient client, int x, int y, int width, int height, List<LogBuilder.Section> sectionList, double scrollAmount, DoubleConsumer scroll) {
		super(x, y, width, height, Text.empty());

		this.grid = new GridWidget();
		this.grid.setRowSpacing(2);
		this.grid.getDefaultSettings().alignHorizontallyLeft();
		this.helper = this.grid.createAdditionHelper(1);
		this.helper.add(SpacerWidget.withWidth(width));
		this.headerSettings = this.helper.copyDefaultSettings().alignHorizontallyCenter().setHorizontalPadding(32);

		for (var section : sectionList) {
			appendHeader(client.textRenderer, section.title());
			for (var text : section.entries()) {
				appendLine(client.textRenderer, text);
			}
			appendSpacer(10);
		}

		this.grid.arrangeElements();
		this.scrollUpdater = scroll;
		this.setScrollAmount(scrollAmount);
	}

	@Override
	protected void setScrollAmount(double scrollAmount) {
		super.setScrollAmount(scrollAmount);
		this.scrollUpdater.accept(scrollAmount);
	}

	public void appendLine(TextRenderer textRenderer, Text text) {
		this.appendLine(textRenderer, text, 0);
	}

	public void appendLine(TextRenderer renderer, Text text, int bottomPadding) {
		this.helper.add((new MultilineTextWidget(text, renderer)).setMaxWidth(this.width), this.helper.copyDefaultSettings().setBottomPadding(bottomPadding));
		this.narration.append(text).append("\n");
	}

	public void appendHeader(TextRenderer renderer, Text text) {
		this.helper.add((new MultilineTextWidget(text, renderer)).setMaxWidth(this.width - 64).setCentered(true), this.headerSettings);
		this.narration.append(text).append("\n");
	}

	public void appendSpacer(int height) {
		this.helper.add(SpacerWidget.withHeight(height));
	}

	@Override
	protected int getContentHeight() {
		return this.grid.getHeight();
	}

	@Override
	protected boolean isOverflowing() {
		return getContentHeight() > this.height;
	}

	@Override
	protected double getScrollRate() {
		return 9.0D;
	}

	@Override
	protected void renderContents(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		int x = this.getY() + this.getInnerPadding();
		int y = this.getX() + this.getInnerPadding();
		matrices.push();
		matrices.translate(y, x, 0.0D);
		this.grid.visitWidgets((clickableWidget) -> {
			clickableWidget.render(matrices, mouseX, mouseY, delta);
		});
		matrices.pop();
	}

	@Override
	protected void updateNarration(NarrationMessageBuilder builder) {

	}
}
