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

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.screen.api.client.ScreenEvents;
import org.quiltmc.qsl.screen.api.client.ScreenKeyboardEvents;
import org.quiltmc.qsl.screen.api.client.ScreenMouseEvents;
import org.quiltmc.qsl.screen.api.client.QuiltScreenHooks;
import org.quiltmc.qsl.screen.impl.client.ButtonList;
import org.quiltmc.qsl.screen.impl.client.ScreenEventFactory;
import org.quiltmc.qsl.screen.impl.client.ScreenExtensions;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.gui.Selectable;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.render.item.ItemRenderer;

@Mixin(Screen.class)
abstract class ScreenMixin implements ScreenExtensions, QuiltScreenHooks {
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
	private MinecraftClient client;

	@Shadow
	private ItemRenderer itemRenderer;

	@Shadow
	private TextRenderer textRenderer;

	@Unique
	private ButtonList quilt$quiltButtons;
	@Unique
	private Event<ScreenEvents.Remove> quilt$removeEvent;
	@Unique
	private Event<ScreenEvents.BeforeTick> quilt$beforeTickEvent;
	@Unique
	private Event<ScreenEvents.AfterTick> quilt$afterTickEvent;
	@Unique
	private Event<ScreenEvents.BeforeRender> quilt$beforeRenderEvent;
	@Unique
	private Event<ScreenEvents.AfterRender> quilt$afterRenderEvent;

	// Keyboard
	@Unique
	private Event<ScreenKeyboardEvents.AllowKeyPress> quilt$allowKeyPressEvent;
	@Unique
	private Event<ScreenKeyboardEvents.BeforeKeyPress> quilt$beforeKeyPressEvent;
	@Unique
	private Event<ScreenKeyboardEvents.AfterKeyPress> quilt$afterKeyPressEvent;
	@Unique
	private Event<ScreenKeyboardEvents.AllowKeyRelease> quilt$allowKeyReleaseEvent;
	@Unique
	private Event<ScreenKeyboardEvents.BeforeKeyRelease> quilt$beforeKeyReleaseEvent;
	@Unique
	private Event<ScreenKeyboardEvents.AfterKeyRelease> quilt$afterKeyReleaseEvent;

	// Mouse
	@Unique
	private Event<ScreenMouseEvents.AllowMouseClick> quilt$allowMouseClickEvent;
	@Unique
	private Event<ScreenMouseEvents.BeforeMouseClick> quilt$beforeMouseClickEvent;
	@Unique
	private Event<ScreenMouseEvents.AfterMouseClick> quilt$afterMouseClickEvent;
	@Unique
	private Event<ScreenMouseEvents.AllowMouseRelease> quilt$allowMouseReleaseEvent;
	@Unique
	private Event<ScreenMouseEvents.BeforeMouseRelease> quilt$beforeMouseReleaseEvent;
	@Unique
	private Event<ScreenMouseEvents.AfterMouseRelease> quilt$afterMouseReleaseEvent;
	@Unique
	private Event<ScreenMouseEvents.AllowMouseScroll> quilt$allowMouseScrollEvent;
	@Unique
	private Event<ScreenMouseEvents.BeforeMouseScroll> quilt$beforeMouseScrollEvent;
	@Unique
	private Event<ScreenMouseEvents.AfterMouseScroll> quilt$afterMouseScrollEvent;

	@Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At("HEAD"))
	private void beforeInitScreen(MinecraftClient client, int width, int height, CallbackInfo ci) {
		// All elements are repopulated on the screen, so we need to reinitialize all events
		this.quilt$quiltButtons = null;
		this.quilt$removeEvent = ScreenEventFactory.createRemoveEvent();
		this.quilt$beforeRenderEvent = ScreenEventFactory.createBeforeRenderEvent();
		this.quilt$afterRenderEvent = ScreenEventFactory.createAfterRenderEvent();
		this.quilt$beforeTickEvent = ScreenEventFactory.createBeforeTickEvent();
		this.quilt$afterTickEvent = ScreenEventFactory.createAfterTickEvent();

		// Keyboard
		this.quilt$allowKeyPressEvent = ScreenEventFactory.createAllowKeyPressEvent();
		this.quilt$beforeKeyPressEvent = ScreenEventFactory.createBeforeKeyPressEvent();
		this.quilt$afterKeyPressEvent = ScreenEventFactory.createAfterKeyPressEvent();
		this.quilt$allowKeyReleaseEvent = ScreenEventFactory.createAllowKeyReleaseEvent();
		this.quilt$beforeKeyReleaseEvent = ScreenEventFactory.createBeforeKeyReleaseEvent();
		this.quilt$afterKeyReleaseEvent = ScreenEventFactory.createAfterKeyReleaseEvent();

		// Mouse
		this.quilt$allowMouseClickEvent = ScreenEventFactory.createAllowMouseClickEvent();
		this.quilt$beforeMouseClickEvent = ScreenEventFactory.createBeforeMouseClickEvent();
		this.quilt$afterMouseClickEvent = ScreenEventFactory.createAfterMouseClickEvent();
		this.quilt$allowMouseReleaseEvent = ScreenEventFactory.createAllowMouseReleaseEvent();
		this.quilt$beforeMouseReleaseEvent = ScreenEventFactory.createBeforeMouseReleaseEvent();
		this.quilt$afterMouseReleaseEvent = ScreenEventFactory.createAfterMouseReleaseEvent();
		this.quilt$allowMouseScrollEvent = ScreenEventFactory.createAllowMouseScrollEvent();
		this.quilt$beforeMouseScrollEvent = ScreenEventFactory.createBeforeMouseScrollEvent();
		this.quilt$afterMouseScrollEvent = ScreenEventFactory.createAfterMouseScrollEvent();

		ScreenEvents.BEFORE_INIT.invoker().beforeInit(client, (Screen) (Object) this, width, height);
	}

	@Inject(method = "init(Lnet/minecraft/client/MinecraftClient;II)V", at = @At("TAIL"))
	private void afterInitScreen(MinecraftClient client, int width, int height, CallbackInfo ci) {
		ScreenEvents.AFTER_INIT.invoker().afterInit(client, (Screen) (Object) this, width, height);
	}

	@Unique
	private <T> Event<T> ensureEventsAreInitialized(Event<T> event) {
		if (event == null) {
			throw new IllegalStateException(String.format("[quilt$screen] The current screen (%s) has not been correctly initialized, please send this crash log to the mod author. This is usually caused by the screen not calling super.init(Lnet/minecraft/client/MinecraftClient;II)V", this.getClass().getSuperclass().getName()));
		}

		return event;
	}

	@Override
	public Event<ScreenEvents.Remove> quilt$getRemoveEvent() {
		return ensureEventsAreInitialized(this.quilt$removeEvent);
	}

	@Override
	public Event<ScreenEvents.BeforeTick> quilt$getBeforeTickEvent() {
		return ensureEventsAreInitialized(this.quilt$beforeTickEvent);
	}

	@Override
	public Event<ScreenEvents.AfterTick> quilt$getAfterTickEvent() {
		return ensureEventsAreInitialized(this.quilt$afterTickEvent);
	}

	@Override
	public Event<ScreenEvents.BeforeRender> quilt$getBeforeRenderEvent() {
		return ensureEventsAreInitialized(this.quilt$beforeRenderEvent);
	}

	@Override
	public Event<ScreenEvents.AfterRender> quilt$getAfterRenderEvent() {
		return ensureEventsAreInitialized(this.quilt$afterRenderEvent);
	}

	// Keyboard

	@Override
	public Event<ScreenKeyboardEvents.AllowKeyPress> quilt$getAllowKeyPressEvent() {
		return ensureEventsAreInitialized(this.quilt$allowKeyPressEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.BeforeKeyPress> quilt$getBeforeKeyPressEvent() {
		return ensureEventsAreInitialized(this.quilt$beforeKeyPressEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.AfterKeyPress> quilt$getAfterKeyPressEvent() {
		return ensureEventsAreInitialized(this.quilt$afterKeyPressEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.AllowKeyRelease> quilt$getAllowKeyReleaseEvent() {
		return ensureEventsAreInitialized(this.quilt$allowKeyReleaseEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.BeforeKeyRelease> quilt$getBeforeKeyReleaseEvent() {
		return ensureEventsAreInitialized(this.quilt$beforeKeyReleaseEvent);
	}

	@Override
	public Event<ScreenKeyboardEvents.AfterKeyRelease> quilt$getAfterKeyReleaseEvent() {
		return ensureEventsAreInitialized(this.quilt$afterKeyReleaseEvent);
	}

	// Mouse

	@Override
	public Event<ScreenMouseEvents.AllowMouseClick> quilt$getAllowMouseClickEvent() {
		return ensureEventsAreInitialized(this.quilt$allowMouseClickEvent);
	}

	@Override
	public Event<ScreenMouseEvents.BeforeMouseClick> quilt$getBeforeMouseClickEvent() {
		return ensureEventsAreInitialized(this.quilt$beforeMouseClickEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AfterMouseClick> quilt$getAfterMouseClickEvent() {
		return ensureEventsAreInitialized(this.quilt$afterMouseClickEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AllowMouseRelease> quilt$getAllowMouseReleaseEvent() {
		return ensureEventsAreInitialized(this.quilt$allowMouseReleaseEvent);
	}

	@Override
	public Event<ScreenMouseEvents.BeforeMouseRelease> quilt$getBeforeMouseReleaseEvent() {
		return ensureEventsAreInitialized(this.quilt$beforeMouseReleaseEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AfterMouseRelease> quilt$getAfterMouseReleaseEvent() {
		return ensureEventsAreInitialized(this.quilt$afterMouseReleaseEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AllowMouseScroll> quilt$getAllowMouseScrollEvent() {
		return ensureEventsAreInitialized(this.quilt$allowMouseScrollEvent);
	}

	@Override
	public Event<ScreenMouseEvents.BeforeMouseScroll> quilt$getBeforeMouseScrollEvent() {
		return ensureEventsAreInitialized(this.quilt$beforeMouseScrollEvent);
	}

	@Override
	public Event<ScreenMouseEvents.AfterMouseScroll> quilt$getAfterMouseScrollEvent() {
		return ensureEventsAreInitialized(this.quilt$afterMouseScrollEvent);
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
		return itemRenderer;
	}

	@Override
	public TextRenderer getTextRenderer() {
		return textRenderer;
	}

	@Override
	public MinecraftClient getClient() {
		return client;
	}
}
