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
 * Events related to use of the keyboard in a {@link Screen}.
 * <p>
 * Events are fired in the following order:
 * <pre>{@code AllowX -> BeforeX -> AfterX}</pre>
 * If the result of the Allow event is {@link TriState#FALSE}, then Before and After are not called.
 *
 * @see ScreenEvents
 */
@ClientOnly
public final class ScreenKeyboardEvents {
	/**
	 * An event that checks if a key press should be allowed.
	 */
	public static final Event<AllowKeyPress> ALLOW_KEY_PRESS = Event.create(AllowKeyPress.class, callbacks -> (screen, key, scancode, modifiers) -> {
		TriState state = TriState.DEFAULT;

		for (var callback : callbacks) {
			state = callback.allowKeyPress(screen, key, scancode, modifiers);

			if (state != TriState.DEFAULT) {
				return state;
			}
		}

		return state;
	});

	/**
	 * An event that is called before a key press is processed for a screen.
	 */
	public static final Event<BeforeKeyPress> BEFORE_KEY_PRESS = Event.create(BeforeKeyPress.class, callbacks -> (screen, key, scancode, modifiers) -> {
		for (var callback : callbacks) {
			callback.beforeKeyPress(screen, key, scancode, modifiers);
		}
	});

	/**
	 * An event that is called after a key press is processed for a screen.
	 */
	public static final Event<AfterKeyPress> AFTER_KEY_PRESS = Event.create(AfterKeyPress.class, callbacks -> (screen, key, scancode, modifiers) -> {
		for (var callback : callbacks) {
			callback.afterKeyPress(screen, key, scancode, modifiers);
		}
	});

	/**
	 * An event that checks if a pressed key should be allowed to release.
	 */
	public static final Event<AllowKeyRelease> ALLOW_KEY_RELEASE = Event.create(AllowKeyRelease.class, callbacks -> (screen, key, scancode, modifiers) -> {
		TriState state = TriState.DEFAULT;

		for (var callback : callbacks) {
			state = callback.allowKeyRelease(screen, key, scancode, modifiers);

			if (state != TriState.DEFAULT) {
				return state;
			}
		}

		return state;
	});

	/**
	 * An event that is called after the release of a key is processed for a screen.
	 */
	public static final Event<BeforeKeyRelease> BEFORE_KEY_RELEASE = Event.create(BeforeKeyRelease.class, callbacks -> (screen, key, scancode, modifiers) -> {
		for (var callback : callbacks) {
			callback.beforeKeyRelease(screen, key, scancode, modifiers);
		}
	});

	/**
	 * An event that is called after the release a key is processed for a screen.
	 */
	public static final Event<AfterKeyRelease> AFTER_KEY_RELEASE = Event.create(AfterKeyRelease.class, callbacks -> (screen, key, scancode, modifiers) -> {
		for (var callback : callbacks) {
			callback.afterKeyRelease(screen, key, scancode, modifiers);
		}
	});

	@ClientOnly
	@FunctionalInterface
	public interface AllowKeyPress extends ClientEventAwareListener {
		/**
		 * Checks if a key should be allowed to be pressed.
		 *
		 * @param key       the named key code which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}
		 * @param scancode  the unique/platform-specific scan code of the keyboard input
		 * @param modifiers a GLFW bitfield describing the modifier keys that are held down
		 * @return whether the key press should be processed
		 * @see org.lwjgl.glfw.GLFW#GLFW_KEY_Q
		 * @see <a href="https://www.glfw.org/docs/3.3/group__mods.html">Modifier key flags</a>
		 */
		TriState allowKeyPress(Screen screen, int key, int scancode, int modifiers);
	}

	@ClientOnly
	@FunctionalInterface
	public interface BeforeKeyPress extends ClientEventAwareListener {
		/**
		 * Called before a key press is handled.
		 *
		 * @param key       the named key code which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}
		 * @param scancode  the unique/platform-specific scan code of the keyboard input
		 * @param modifiers a GLFW bitfield describing the modifier keys that are held down
		 * @see org.lwjgl.glfw.GLFW#GLFW_KEY_Q
		 * @see <a href="https://www.glfw.org/docs/3.3/group__mods.html">Modifier key flags</a>
		 */
		void beforeKeyPress(Screen screen, int key, int scancode, int modifiers);
	}

	@ClientOnly
	@FunctionalInterface
	public interface AfterKeyPress extends ClientEventAwareListener {
		/**
		 * Called after a key press is handled.
		 *
		 * @param key       the named key code which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}
		 * @param scancode  the unique/platform-specific scan code of the keyboard input
		 * @param modifiers a GLFW bitfield describing the modifier keys that are held down
		 * @see org.lwjgl.glfw.GLFW#GLFW_KEY_Q
		 * @see <a href="https://www.glfw.org/docs/3.3/group__mods.html">Modifier key flags</a>
		 */
		void afterKeyPress(Screen screen, int key, int scancode, int modifiers);
	}

	@ClientOnly
	@FunctionalInterface
	public interface AllowKeyRelease extends ClientEventAwareListener {
		/**
		 * Checks if a pressed key should be allowed to be released.
		 *
		 * @param key       the named key code which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}
		 * @param scancode  the unique/platform-specific scan code of the keyboard input
		 * @param modifiers a GLFW bitfield describing the modifier keys that are held down
		 * @return whether the key press should be released
		 * @see org.lwjgl.glfw.GLFW#GLFW_KEY_Q
		 * @see <a href="https://www.glfw.org/docs/3.3/group__mods.html">Modifier key flags</a>
		 */
		TriState allowKeyRelease(Screen screen, int key, int scancode, int modifiers);
	}

	@ClientOnly
	@FunctionalInterface
	public interface BeforeKeyRelease extends ClientEventAwareListener {
		/**
		 * Called before a pressed key has been released.
		 *
		 * @param key       the named key code which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}
		 * @param scancode  the unique/platform-specific scan code of the keyboard input
		 * @param modifiers a GLFW bitfield describipackage org.quiltmc.qsl.screen.api.client;ng the modifier keys that are held down
		 * @see org.lwjgl.glfw.GLFW#GLFW_KEY_Q
		 * @see <a href="https://www.glfw.org/docs/3.3/group__mods.html">Modifier key flags</a>
		 */
		void beforeKeyRelease(Screen screen, int key, int scancode, int modifiers);
	}

	@ClientOnly
	@FunctionalInterface
	public interface AfterKeyRelease extends ClientEventAwareListener {
		/**
		 * Called after a pressed key has been released.
		 *
		 * @param key       the named key code which can be identified by the constants in {@link org.lwjgl.glfw.GLFW GLFW}
		 * @param scancode  the unique/platform-specific scan code of the keyboard input
		 * @param modifiers a GLFW bitfield describing the modifier keys that are held down
		 * @see org.lwjgl.glfw.GLFW#GLFW_KEY_Q
		 * @see <a href="https://www.glfw.org/docs/3.3/group__mods.html">Modifier key flags</a>
		 */
		void afterKeyRelease(Screen screen, int key, int scancode, int modifiers);
	}
}
