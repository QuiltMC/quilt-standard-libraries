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

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screen.Screen;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.client.ClientEventAwareListener;

/**
 * Holds events related to {@link Screen}s.
 * <p>
 * A screen being (re)initialized will reset the screen to its default state,
 * therefore reverting all changes a mod developer may have applied to a screen.
 * <p>
 * The primary entrypoint into a screen is when it is being opened, this is signified by an event {@link ScreenEvents#BEFORE_INIT before}
 * and {@link ScreenEvents#AFTER_INIT after} initialization of the screen.
 *
 * @see QuiltScreen
 * @see ScreenKeyboardEvents
 * @see ScreenMouseEvents
 */
@ClientOnly
public final class ScreenEvents {
	/**
	 * An event that is called before a screen is initialized to its default state.
	 * <p>
	 * This event indicates that a screen with no special handling of element repositioning has been resized, and therefore
	 * is being re-initialized.
	 * This event can also indicate that the previous screen has been changed.
	 *
	 * @see ScreenEvents#AFTER_INIT
	 */
	public static final Event<BeforeInit> BEFORE_INIT = Event.create(BeforeInit.class, callbacks -> (screen, client, firstInit) -> {
		for (var callback : callbacks) {
			callback.beforeInit(screen, client, firstInit);
		}
	});

	/**
	 * An event that is called after a screen is initialized to its default state.
	 * <p>
	 * Typically, this event is used to modify a screen after the screen has been initialized.
	 * Modifications such as changing sizes of buttons, removing buttons and adding/removing child elements to the screen
	 * can be done safely using this event.
	 * <p>
	 * This event can also indicate that the previous screen has been closed.
	 * <p>
	 * Note that by adding an element to a screen, the element is not automatically {@link net.minecraft.client.gui.screen.Screen ticked} or {@link net.minecraft.client.gui.Drawable drawn}.
	 * Unless the element is a button, you need to call the specific {@link Screen#tick() tick} and {@link net.minecraft.client.gui.Drawable#render(GuiGraphics, int, int, float) render} methods in the corresponding screen events.
	 * <p>
	 * For example, to add a button to the title screen, the following code could be used:
	 * <pre>{@code
	 * ScreenEvents.AFTER_INIT.register((screen, client, firstInit) -> {
	 * 	if (screen instanceof TitleScreen) {
	 * 		screen.getButtons().add(ButtonWidget.builder(...).build());
	 *    }
	 * });
	 * }</pre>
	 *
	 * @see ScreenEvents#BEFORE_INIT
	 */
	public static final Event<AfterInit> AFTER_INIT = Event.create(AfterInit.class, callbacks -> (screen, client, firstInit) -> {
		for (var callback : callbacks) {
			callback.afterInit(screen, client, firstInit);
		}
	});

	/**
	 * An event that is called after {@link Screen#removed()} is called.
	 * This event signifies that the screen is now closed.
	 * <p>
	 * This event is typically used to undo any screen specific state changes such as
	 * terminate threads spawned by a screen.
	 * This event may precede initialization events {@link ScreenEvents#BEFORE_INIT} but there is no guarantee that event will be called immediately afterwards.
	 */
	public static final Event<Remove> REMOVE = Event.create(Remove.class, callbacks -> screen -> {
		for (var callback : callbacks) {
			callback.onRemove(screen);
		}
	});

	/**
	 * An event that is called before a screen is rendered.
	 */
	public static final Event<BeforeRender> BEFORE_RENDER = Event.create(BeforeRender.class, callbacks -> (screen, graphics, mouseX, mouseY, tickDelta) -> {
		for (var callback : callbacks) {
			callback.beforeRender(screen, graphics, mouseX, mouseY, tickDelta);
		}
	});

	/**
	 * An event that is called after a screen is rendered.
	 */
	public static final Event<AfterRender> AFTER_RENDER = Event.create(AfterRender.class, callbacks -> (screen, graphics, mouseX, mouseY, tickDelta) -> {
		for (var callback : callbacks) {
			callback.afterRender(screen, graphics, mouseX, mouseY, tickDelta);
		}
	});

	/**
	 * An event that is called before a screen is ticked.
	 */
	public static final Event<BeforeTick> BEFORE_TICK = Event.create(BeforeTick.class, callbacks -> screen -> {
		for (var callback : callbacks) {
			callback.beforeTick(screen);
		}
	});

	/**
	 * An event that is called after a screen is ticked.
	 */
	public static final Event<AfterTick> AFTER_TICK = Event.create(AfterTick.class, callbacks -> screen -> {
		for (var callback : callbacks) {
			callback.afterTick(screen);
		}
	});

	@ClientOnly
	@FunctionalInterface
	public interface BeforeInit extends ClientEventAwareListener {
		/**
		 * An event that is called before a screen is initialized to its default state.
		 *
		 * @param screen the screen
		 * @param client the screen's {@link MinecraftClient client} instance
		 * @param firstInit {@code true} if the screen has been initialized for the first time, or {@code false} otherwise
		 */
		void beforeInit(Screen screen, MinecraftClient client, boolean firstInit);
	}

	@ClientOnly
	@FunctionalInterface
	public interface AfterInit extends ClientEventAwareListener {
		/**
		 * An event that is called after a screen is initialized to its default state.
		 *
		 * @param screen the screen
		 * @param client the screen's client instance
		 * @param firstInit {@code true} if the screen has been initialized for the first time, or {@code false} otherwise
		 */
		void afterInit(Screen screen, MinecraftClient client, boolean firstInit);
	}

	@ClientOnly
	@FunctionalInterface
	public interface Remove extends ClientEventAwareListener {
		void onRemove(Screen screen);
	}

	@ClientOnly
	@FunctionalInterface
	public interface BeforeRender extends ClientEventAwareListener {
		void beforeRender(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float tickDelta);
	}

	@ClientOnly
	@FunctionalInterface
	public interface AfterRender extends ClientEventAwareListener {
		void afterRender(Screen screen, GuiGraphics graphics, int mouseX, int mouseY, float tickDelta);
	}

	@ClientOnly
	@FunctionalInterface
	public interface BeforeTick extends ClientEventAwareListener {
		void beforeTick(Screen screen);
	}

	@ClientOnly
	@FunctionalInterface
	public interface AfterTick extends ClientEventAwareListener {
		void afterTick(Screen screen);
	}

	private ScreenEvents() {}
}
