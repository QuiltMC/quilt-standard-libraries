/*
 * Copyright 2024 The Quilt Project
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

package org.quiltmc.qsl.resource.loader.impl.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.pack.PackScreen;
import net.minecraft.client.gui.widget.list.pack.PackEntryListWidget;

import org.quiltmc.qsl.resource.loader.impl.QuiltBuiltinPackProfile;
import org.quiltmc.qsl.resource.loader.mixin.client.PackScreenAccessor;
import org.quiltmc.qsl.resource.loader.mixin.client.ResourcePackEntryAccessor;
import org.quiltmc.qsl.screen.api.client.QuiltScreen;
import org.quiltmc.qsl.screen.api.client.ScreenEvents;

public class PackScreenTooltips implements ScreenEvents.AfterRender {
	@Override
	public void afterRender(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float tickDelta) {
		if (screen instanceof PackScreen packScreen) {
			PackEntryListWidget.PackEntry availableEntry = ((PackScreenAccessor) packScreen).getAvailablePackList().getHoveredEntry();
			if (availableEntry != null) {
				if (((ResourcePackEntryAccessor) availableEntry).getPack().getSource() instanceof QuiltBuiltinPackProfile.BuiltinPackSource source) {
					graphics.drawTooltip(((QuiltScreen) packScreen).getTextRenderer(), source.getTooltip(), mouseX, mouseY);
				}
			}

			PackEntryListWidget.PackEntry selectedEntry = ((PackScreenAccessor) packScreen).getSelectedPackList().getHoveredEntry();
			if (selectedEntry != null) {
				if (((ResourcePackEntryAccessor) selectedEntry).getPack().getSource() instanceof QuiltBuiltinPackProfile.BuiltinPackSource source) {
					graphics.drawTooltip(((QuiltScreen) packScreen).getTextRenderer(), source.getTooltip(), mouseX, mouseY);
				}
			}
		}
	}
}
