/*
 * Copyright 2023 The Quilt Project
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

import java.util.function.DoubleConsumer;

import org.jetbrains.annotations.ApiStatus;

import net.minecraft.client.gui.widget.MultilineScrollableWidget;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

import org.quiltmc.loader.api.minecraft.ClientOnly;

// TODO: When MultilineScrollableWidget's parent classes are mapped, discuss re-doing this
@ApiStatus.Internal
@ClientOnly
public class ScrollableMultiTextWidget extends MultilineScrollableWidget {
	private final DoubleConsumer scrollUpdater;

	public ScrollableMultiTextWidget(
			int x, int y, int width, int height, Text text, TextRenderer textRenderer, double scroll,
			DoubleConsumer scrollUpdater
	) {
		super(x, y, width, height, text, textRenderer);
		this.scrollUpdater = scrollUpdater;

		this.method_44382(scroll);
	}

	// Originally setScrollAmount
	@Override
	public void method_44382(double scrollAmount) {
		super.method_44382(scrollAmount);
		this.scrollUpdater.accept(scrollAmount);
	}
}
