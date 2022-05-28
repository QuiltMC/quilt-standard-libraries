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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

import org.quiltmc.qsl.base.api.event.Event;
import org.quiltmc.qsl.base.api.event.client.ClientEventAwareListener;

/**
 * Holds events related to {@link Screen}s.
 *
 * <p>A screen being (re)initialized will reset the screen to its default state,
 * therefore reverting all changes a mod developer may have applied to a screen.
 *
 * <p>The primary entrypoint into a screen is when it is being opened, this is signified by an event {@link ScreenEvents#BEFORE_INIT before}
 * and {@link ScreenEvents#AFTER_INIT after} initialization of the screen.
 *
 * @see QuiltScreen
 * @see ScreenKeyboardEvents
 * @see ScreenMouseEvents
 */
@Environment(EnvType.CLIENT)
public final class ScreenEvents {
	/**
	 * An event that is called before {@link Screen#init(MinecraftClient, int, int) a screen is initialized} to its default state.
	 * It should be noted that some methods in {@link QuiltScreen} such
	 * as a screen's {@link QuiltScreen#getTextRenderer() text renderer} may not be initialized yet,
	 * and as such their use is discouraged.
	 * <p>
	 * This event indicates a screen has been resized, and therefore is being re-initialized.
	 * This event can also indicate that the previous screen has been changed.
	 *
	 * @see ScreenEvents#AFTER_INIT
	 */
	public static final Event<BeforeInit> BEFORE_INIT = Event.create(BeforeInit.class, callbacks -> (client, screen, scaledWidth, scaledHeight) -> {
		for (var callback : callbacks) {
			callback.beforeInit(client, screen, scaledWidth, scaledHeight);
		}
	});

	/**
	 * An event that is called after {@link Screen#init(MinecraftClient, int, int) a screen is initialized} to its default state.
	 * <p>
	 * Typically, this event is used to modify a screen after the screen has been initialized.
	 * Modifications such as changing sizes of buttons, removing buttons and adding/removing child elements to the screen
	 * can be done safely using this event.
	 * <p>
	 * This event can also indicate that the previous screen has been closed.
	 * <p>
	 * Note that by adding an element to a screen, the element is not automatically {@link net.minecraft.client.gui.screen.Screen ticked} or {@link net.minecraft.client.gui.Drawable drawn}.
	 * Unless the element is a button, you need to call the specific {@link Screen#tick() tick} and {@link net.minecraft.client.gui.Drawable#render(MatrixStack, int, int, float) render} methods in the corresponding screen events.
	 * <p>
	 * For example, to add a button to the title screen, the following code could be used:
	 * <pre>{@code
	 * ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
	 * 	if (screen instanceof TitleScreen) {
	 * 		screen.getButtons().add(new ButtonWidget(...));
	 *    }
	 * });
	 * }</pre>
	 *
	 * @see ScreenEvents#BEFORE_INIT
	 */
	public static final Event<AfterInit> AFTER_INIT = Event.create(AfterInit.class, callbacks -> (client, screen, scaledWidth, scaledHeight) -> {
		for (var callback : callbacks) {
			callback.afterInit(client, screen, scaledWidth, scaledHeight);
		}
	});

	/**
	 * An event that is called after {@link Screen#removed()} is called.
	 * This event signifies that the screen is now closed.
	 * <p>
	 * This event is typically used to undo any screen specific state changes such as
	 * setting the keyboard to receive {@link net.minecraft.client.Keyboard#setRepeatEvents(boolean) repeat events}
	 * or terminate threads spawned by a screen.
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
	public static final Event<BeforeRender> BEFORE_RENDER = Event.create(BeforeRender.class, callbacks -> (screen, matrices, mouseX, mouseY, tickDelta) -> {
		for (var callback : callbacks) {
			callback.beforeRender(screen, matrices, mouseX, mouseY, tickDelta);
		}
	});

	/**
	 * An event that is called after a screen is rendered.
	 */
	public static final Event<AfterRender> AFTER_RENDER = Event.create(AfterRender.class, callbacks -> (screen, matrices, mouseX, mouseY, tickDelta) -> {
		for (var callback : callbacks) {
			callback.afterRender(screen, matrices, mouseX, mouseY, tickDelta);
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

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface BeforeInit extends ClientEventAwareListener {
		void beforeInit(Screen screen, MinecraftClient client, int scaledWidth, int scaledHeight);
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface AfterInit extends ClientEventAwareListener {
		void afterInit(Screen screen, MinecraftClient client, int scaledWidth, int scaledHeight);
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface Remove extends ClientEventAwareListener {
		void onRemove(Screen screen);
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface BeforeRender extends ClientEventAwareListener {
		void beforeRender(Screen screen, MatrixStack matrices, int mouseX, int mouseY, float tickDelta);
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface AfterRender extends ClientEventAwareListener {
		void afterRender(Screen screen, MatrixStack matrices, int mouseX, int mouseY, float tickDelta);
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface BeforeTick extends ClientEventAwareListener {
		void beforeTick(Screen screen);
	}

	@Environment(EnvType.CLIENT)
	@FunctionalInterface
	public interface AfterTick extends ClientEventAwareListener {
		void afterTick(Screen screen);
	}

	private ScreenEvents() { }
}
