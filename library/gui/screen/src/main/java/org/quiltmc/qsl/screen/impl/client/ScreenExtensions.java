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

package org.quiltmc.qsl.screen.impl.client;

import java.util.List;

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.screen.api.client.ScreenEvents;
import org.quiltmc.qsl.screen.api.client.ScreenKeyboardEvents;
import org.quiltmc.qsl.screen.api.client.ScreenMouseEvents;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public interface ScreenExtensions {
	static ScreenExtensions getExtensions(Screen screen) {
		return (ScreenExtensions) screen;
	}

	List<ClickableWidget> quilt_getButtons();

	Event<ScreenEvents.Remove> quilt_getRemoveEvent();

	Event<ScreenEvents.BeforeTick> quilt_getBeforeTickEvent();

	Event<ScreenEvents.AfterTick> quilt_getAfterTickEvent();

	Event<ScreenEvents.BeforeRender> quilt_getBeforeRenderEvent();

	Event<ScreenEvents.AfterRender> quilt_getAfterRenderEvent();

	// Keyboard

	Event<ScreenKeyboardEvents.AllowKeyPress> quilt_getAllowKeyPressEvent();

	Event<ScreenKeyboardEvents.BeforeKeyPress> quilt_getBeforeKeyPressEvent();

	Event<ScreenKeyboardEvents.AfterKeyPress> quilt_getAfterKeyPressEvent();

	Event<ScreenKeyboardEvents.AllowKeyRelease> quilt_getAllowKeyReleaseEvent();

	Event<ScreenKeyboardEvents.BeforeKeyRelease> quilt_getBeforeKeyReleaseEvent();

	Event<ScreenKeyboardEvents.AfterKeyRelease> quilt_getAfterKeyReleaseEvent();

	// Mouse

	Event<ScreenMouseEvents.AllowMouseClick> quilt_getAllowMouseClickEvent();

	Event<ScreenMouseEvents.BeforeMouseClick> quilt_getBeforeMouseClickEvent();

	Event<ScreenMouseEvents.AfterMouseClick> quilt_getAfterMouseClickEvent();

	Event<ScreenMouseEvents.AllowMouseRelease> quilt_getAllowMouseReleaseEvent();

	Event<ScreenMouseEvents.BeforeMouseRelease> quilt_getBeforeMouseReleaseEvent();

	Event<ScreenMouseEvents.AfterMouseRelease> quilt_getAfterMouseReleaseEvent();

	Event<ScreenMouseEvents.AllowMouseScroll> quilt_getAllowMouseScrollEvent();

	Event<ScreenMouseEvents.BeforeMouseScroll> quilt_getBeforeMouseScrollEvent();

	Event<ScreenMouseEvents.AfterMouseScroll> quilt_getAfterMouseScrollEvent();
}
