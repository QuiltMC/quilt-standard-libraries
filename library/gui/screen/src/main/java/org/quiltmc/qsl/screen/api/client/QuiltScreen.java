/*
 * Copyright 2016, 2017, 2018, 2019 FabricMC
 * Copyright 2022 The Quilt Project
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

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.util.InjectedInterface;

/**
 * An interface implemented by {@link Screen} through a mixin in order to expose QSL extensions and also provide utility methods.
 *
 * @see ScreenEvents
 */
@ClientOnly
@InjectedInterface(Screen.class)
public interface QuiltScreen {
	/**
	 * Gets all the screen's button widgets.
	 * <p>
	 * The provided list allows for addition and removal of buttons from the screen.
	 * This method should be preferred over adding buttons directly to a screen's {@link Screen#children() child elements}.
	 *
	 * @return a list of all the screen's buttons
	 */
	default List<ClickableWidget> getButtons() {
		throw new UnsupportedOperationException("No implementation of getButtons could be found.");
	}

	/**
	 * {@return the screen's text renderer}
	 */
	default TextRenderer getTextRenderer() {
		throw new UnsupportedOperationException("No implementation of getTextRenderer could be found.");
	}

	/**
	 * {@return the Minecraft client instance}
	 */
	default MinecraftClient getClient() {
		throw new UnsupportedOperationException("No implementation of getClient could be found.");
	}
}
