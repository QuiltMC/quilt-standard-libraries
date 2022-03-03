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

import org.jetbrains.annotations.ApiStatus;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.screen.api.client.ScreenEvents;
import org.quiltmc.qsl.screen.api.client.ScreenKeyboardEvents;
import org.quiltmc.qsl.screen.api.client.ScreenMouseEvents;

import net.minecraft.client.gui.screen.Screen;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@ApiStatus.Internal
@Environment(EnvType.CLIENT)
public interface ScreenExtensions {
	static ScreenExtensions getExtensions(Screen screen) {
		return (ScreenExtensions) screen;
	}

	Event<ScreenEvents.Remove> quilt$getRemoveEvent();

	Event<ScreenEvents.BeforeTick> quilt$getBeforeTickEvent();

	Event<ScreenEvents.AfterTick> quilt$getAfterTickEvent();

	Event<ScreenEvents.BeforeRender> quilt$getBeforeRenderEvent();

	Event<ScreenEvents.AfterRender> quilt$getAfterRenderEvent();

	// Keyboard

	Event<ScreenKeyboardEvents.AllowKeyPress> quilt$getAllowKeyPressEvent();

	Event<ScreenKeyboardEvents.BeforeKeyPress> quilt$getBeforeKeyPressEvent();

	Event<ScreenKeyboardEvents.AfterKeyPress> quilt$getAfterKeyPressEvent();

	Event<ScreenKeyboardEvents.AllowKeyRelease> quilt$getAllowKeyReleaseEvent();

	Event<ScreenKeyboardEvents.BeforeKeyRelease> quilt$getBeforeKeyReleaseEvent();

	Event<ScreenKeyboardEvents.AfterKeyRelease> quilt$getAfterKeyReleaseEvent();

	// Mouse

	Event<ScreenMouseEvents.AllowMouseClick> quilt$getAllowMouseClickEvent();

	Event<ScreenMouseEvents.BeforeMouseClick> quilt$getBeforeMouseClickEvent();

	Event<ScreenMouseEvents.AfterMouseClick> quilt$getAfterMouseClickEvent();

	Event<ScreenMouseEvents.AllowMouseRelease> quilt$getAllowMouseReleaseEvent();

	Event<ScreenMouseEvents.BeforeMouseRelease> quilt$getBeforeMouseReleaseEvent();

	Event<ScreenMouseEvents.AfterMouseRelease> quilt$getAfterMouseReleaseEvent();

	Event<ScreenMouseEvents.AllowMouseScroll> quilt$getAllowMouseScrollEvent();

	Event<ScreenMouseEvents.BeforeMouseScroll> quilt$getBeforeMouseScrollEvent();

	Event<ScreenMouseEvents.AfterMouseScroll> quilt$getAfterMouseScrollEvent();
}
