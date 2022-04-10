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

package org.quiltmc.qsl.screen.api.client;

import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.item.ItemRenderer;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

/**
 * Utility methods related to screens.
 *
 * @see ScreenEvents
 */
@Environment(EnvType.CLIENT)
public interface QuiltScreenExtensions {
	/**
	 * Gets all of a screen's button widgets.
	 * The provided list allows for addition and removal of buttons from the screen.
	 * This method should be preferred over adding buttons directly to a screen's {@link Screen#children() child elements}.
	 *
	 * @return a list of all of a screen's buttons
	 */
	List<ClickableWidget> getButtons();

	/**
	 * Gets a screen's item renderer.
	 *
	 * @return the screen's item renderer
	 */
	ItemRenderer getItemRenderer();

	/**
	 * Gets a screen's text renderer.
	 *
	 * @return the screen's text renderer.
	 */
	TextRenderer getTextRenderer();

	MinecraftClient getClient();
}
