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

import net.minecraft.client.gui.screen.Screen;

import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.client.ClientEventAwareListener;
import org.quiltmc.qsl.base.api.util.TriState;

/**
 * Events related to use of the mouse in a {@link Screen}.
 * <p>
 * Events are fired in the following order:
 * <pre>{@code AllowX -> BeforeX -> AfterX}</pre>
 * If the result of the Allow event is {@link TriState#FALSE}, then Before and After are not called.
 *
 * @see ScreenEvents
 */
@ClientOnly
public final class ScreenMouseEvents {
	/**
	 * An event that checks if the mouse click should be allowed.
	 */
	public static final Event<AllowMouseClick> ALLOW_MOUSE_CLICK = Event.create(AllowMouseClick.class, callbacks -> (screen, mouseX, mouseY, button) -> {
		TriState state = TriState.DEFAULT;

		for (var callback : callbacks) {
			state = callback.allowMouseClick(screen, mouseX, mouseY, button);

			if (state != TriState.DEFAULT) {
				return state;
			}
		}

		return state;
	});

	/**
	 * An event that is called before a mouse click is processed for a screen.
	 */
	public static final Event<BeforeMouseClick> BEFORE_MOUSE_CLICK = Event.create(BeforeMouseClick.class, callbacks -> (screen, mouseX, mouseY, button) -> {
		for (var callback : callbacks) {
			callback.beforeMouseClick(screen, mouseX, mouseY, button);
		}
	});

	/**
	 * An event that is called after a mouse click is processed for a screen.
	 */
	public static final Event<AfterMouseClick> AFTER_MOUSE_CLICK = Event.create(AfterMouseClick.class, callbacks -> (screen, mouseX, mouseY, button) -> {
		for (var callback : callbacks) {
			callback.afterMouseClick(screen, mouseX, mouseY, button);
		}
	});

	/**
	 * An event that checks if the mouse click should be allowed to release in a screen.
	 */
	public static final Event<AllowMouseRelease> ALLOW_MOUSE_RELEASE = Event.create(AllowMouseRelease.class, callbacks -> (screen, mouseX, mouseY, button) -> {
		TriState state = TriState.DEFAULT;

		for (var callback : callbacks) {
			state = callback.allowMouseRelease(screen, mouseX, mouseY, button);

			if (state != TriState.DEFAULT) {
				return state;
			}
		}

		return state;
	});

	/**
	 * An event that is called before the release of a mouse click is processed for a screen.
	 */
	public static final Event<BeforeMouseRelease> BEFORE_MOUSE_RELEASE = Event.create(BeforeMouseRelease.class, callbacks -> (screen, mouseX, mouseY, button) -> {
		for (var callback : callbacks) {
			callback.beforeMouseRelease(screen, mouseX, mouseY, button);
		}
	});

	/**
	 * An event that is called after the release of a mouse click is processed for a screen.
	 */
	public static final Event<AfterMouseRelease> AFTER_MOUSE_RELEASE = Event.create(AfterMouseRelease.class, callbacks -> (screen, mouseX, mouseY, button) -> {
		for (var callback : callbacks) {
			callback.afterMouseRelease(screen, mouseX, mouseY, button);
		}
	});

	/**
	 * An event that is checks if the mouse should be allowed to scroll in a screen.
	 * <p>
	 * This event tracks amount of vertical and horizontal scroll.
	 */
	public static final Event<AllowMouseScroll> ALLOW_MOUSE_SCROLL = Event.create(AllowMouseScroll.class, callbacks -> (screen, mouseX, mouseY, scrollDistanceX, scrollDistanceY) -> {
		TriState state = TriState.DEFAULT;

		for (var callback : callbacks) {
			state = callback.allowMouseScroll(screen, mouseX, mouseY, scrollDistanceX, scrollDistanceY);

			if (state != TriState.DEFAULT) {
				return state;
			}
		}

		return state;
	});

	/**
	 * An event that is called after mouse scrolling is processed for a screen.
	 * <p>
	 * This event tracks amount of vertical and horizontal scroll.
	 */
	public static final Event<BeforeMouseScroll> BEFORE_MOUSE_SCROLL = Event.create(BeforeMouseScroll.class, callbacks -> (screen, mouseX, mouseY, scrollDistanceX, scrollDistanceY) -> {
		for (var callback : callbacks) {
			callback.beforeMouseScroll(screen, mouseX, mouseY, scrollDistanceX, scrollDistanceY);
		}
	});

	/**
	 * An event that is called after mouse scrolling is processed for a screen.
	 * <p>
	 * This event tracks amount a mouse was scrolled both vertically and horizontally.
	 */
	public static final Event<AfterMouseScroll> AFTER_MOUSE_SCROLL = Event.create(AfterMouseScroll.class, callbacks -> (screen, mouseX, mouseY, scrollDistanceX, scrollDistanceY) -> {
		for (var callback : callbacks) {
			callback.afterMouseScroll(screen, mouseX, mouseY, scrollDistanceX, scrollDistanceY);
		}
	});

	@ClientOnly
	@FunctionalInterface
	public interface AllowMouseClick extends ClientEventAwareListener {
		/**
		 * Checks if the mouse click should be allowed to be pressed in a screen.
		 *
		 * @param mouseX the X position of the mouse
		 * @param mouseY the Y position of the mouse
		 * @param button the button number, which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}.
		 * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
		 */
		TriState allowMouseClick(Screen screen, double mouseX, double mouseY, int button);
	}

	@ClientOnly
	@FunctionalInterface
	public interface BeforeMouseClick extends ClientEventAwareListener {
		/**
		 * Called before a mouse click has been pressed in a screen.
		 *
		 * @param mouseX the X position of the mouse
		 * @param mouseY the Y position of the mouse
		 * @param button the button number, which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}.
		 * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
		 */
		void beforeMouseClick(Screen screen, double mouseX, double mouseY, int button);
	}

	@ClientOnly
	@FunctionalInterface
	public interface AfterMouseClick extends ClientEventAwareListener {
		/**
		 * Called after a mouse click has been pressed in a screen.
		 *
		 * @param mouseX the X position of the mouse
		 * @param mouseY the Y position of the mouse
		 * @param button the button number, which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}.
		 * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
		 */
		void afterMouseClick(Screen screen, double mouseX, double mouseY, int button);
	}

	@ClientOnly
	@FunctionalInterface
	public interface AllowMouseRelease extends ClientEventAwareListener {
		/**
		 * Checks if the mouse click should be allowed to release in a screen.
		 *
		 * @param mouseX the X position of the mouse
		 * @param mouseY the Y position of the mouse
		 * @param button the button number, which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}.
		 * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
		 */
		TriState allowMouseRelease(Screen screen, double mouseX, double mouseY, int button);
	}

	@ClientOnly
	@FunctionalInterface
	public interface BeforeMouseRelease extends ClientEventAwareListener {
		/**
		 * Called before a mouse click has released in a screen.
		 *
		 * @param mouseX the X position of the mouse
		 * @param mouseY the Y position of the mouse
		 * @param button the button number, which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}.
		 * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
		 */
		void beforeMouseRelease(Screen screen, double mouseX, double mouseY, int button);
	}

	@ClientOnly
	@FunctionalInterface
	public interface AfterMouseRelease extends ClientEventAwareListener {
		/**
		 * Called after a mouse click has released in a screen.
		 *
		 * @param mouseX the X position of the mouse
		 * @param mouseY the Y position of the mouse
		 * @param button the button number, which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}.
		 * @see org.lwjgl.glfw.GLFW#GLFW_MOUSE_BUTTON_1
		 */
		void afterMouseRelease(Screen screen, double mouseX, double mouseY, int button);
	}

	@ClientOnly
	@FunctionalInterface
	public interface AllowMouseScroll extends ClientEventAwareListener {
		/**
		 * Checks if the mouse should be allowed to scroll in a screen.
		 *
		 * @param mouseX          the X position of the mouse
		 * @param mouseY          the Y position of the mouse
		 * @param scrollDistanceX the horizontal scroll distance
		 * @param scrollDistanceY the vertical scroll distance
		 * @return whether the mouse should be allowed to scroll
		 */
		TriState allowMouseScroll(Screen screen, double mouseX, double mouseY, double scrollDistanceX, double scrollDistanceY);
	}

	@ClientOnly
	@FunctionalInterface
	public interface BeforeMouseScroll extends ClientEventAwareListener {
		/**
		 * Called before a mouse has scrolled on screen.
		 *
		 * @param mouseX          the X position of the mouse
		 * @param mouseY          the Y position of the mouse
		 * @param scrollDistanceX the horizontal scroll distance
		 * @param scrollDistanceY the vertical scroll distance
		 */
		void beforeMouseScroll(Screen screen, double mouseX, double mouseY, double scrollDistanceX, double scrollDistanceY);
	}

	@ClientOnly
	@FunctionalInterface
	public interface AfterMouseScroll extends ClientEventAwareListener {
		/**
		 * Called after a mouse has scrolled on screen.
		 *
		 * @param mouseX          the X position of the mouse
		 * @param mouseY          the Y position of the mouse
		 * @param scrollDistanceX the horizontal scroll distance
		 * @param scrollDistanceY the vertical scroll distance
		 */
		void afterMouseScroll(Screen screen, double mouseX, double mouseY, double scrollDistanceX, double scrollDistanceY);
	}
}
